package org.exastax.poiimporter.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exastax.poiimporter.conf.AppConfig;
import org.springframework.core.env.Environment;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

//Manager for CVS File Operations.
public class FileManager implements IFileManager {

    //Parameters
    private final Log log = LogFactory.getLog ( FileManager.class );
    private BufferedReader brPoi, brCity, brTown;
    private List <String> headers;
    private HashMap <String, String> cityList = new HashMap <> ( );
    private HashMap <String, String> townList = new HashMap <> ( );
    private int cityColumnIndex, townColumnIndex;


    //Constructor
    public FileManager(Environment env) throws Exception {
        AppConfig.properties = new PropertiesModel ( env );
        brPoi = getFileContent(AppConfig.properties.getFilePathPoi());
        getCityTownLists ( );
    }

    // Get city and town list from file file and convert to hashmap
    private void getCityTownLists() throws Exception {
        try {
            brCity = getFileContent(AppConfig.properties.getFilePathCity ());
            brTown = getFileContent(AppConfig.properties.getFilePathTown ());
            getMapFromCSVString ();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace ( );
        }
    }

    // Convert file content to HashMap by predefined seperator
    private void getMapFromCSVString() throws Exception {
        brCity.readLine ( );
        brTown.readLine ( );
        List <String> columns;
        while ((columns = line ( brCity, AppConfig.properties.getCharSeparatorCity ( ), 2 )) != null) {
            cityList.put ( columns.get ( 0 ), columns.get ( 1 ) );
        }
        while ((columns = line ( brTown, AppConfig.properties.getCharSeparatorTown ( ), 2 )) != null) {
            townList.put ( columns.get ( 0 ), columns.get ( 1 ) );
        }
    }

    // Split line by given seperator
    private List <String> columns(String line, String sperator, int columnCount) {
        List <String> columns = new ArrayList <> ( );
        Scanner       scan    = new Scanner ( line );
        if (sperator.equals ( "|" ))
            scan.useDelimiter ( "\\" + sperator );
        else
            scan.useDelimiter ( sperator );
        if (columnCount != 0) {
            int k = 0;
            while (scan.hasNext ( ) && k < columnCount) {
                columns.add ( scan.next ( ).trim ( ) );
                k++;
            }
        } else
            while (scan.hasNext ( )) {
                columns.add ( scan.next ( ).trim ( ) );
            }
        scan.close ( );
        return columns;
    }

    //Add foreign columns to poi index
    private void addForeignColumns(List <String> columns) throws Exception {
        try {
            columns.add ( cityList.get ( "" + columns.get ( cityColumnIndex ) ) );
            columns.add ( townList.get ( "" + columns.get ( townColumnIndex ) ) );
        } catch (Exception ex) {
            log.error ( String.format ( "Unable to find cityColumnIndex:%d or townColumnIndex:%d. Exception:%s",
                    cityColumnIndex, townColumnIndex, ex.getMessage ( ) ) );
        }
    }
    // Get poi cvs file header

    @Override
    public List <String> getHeadersPoi() throws Exception {
        String line = brPoi.readLine ( );
        line = line.replace ( "\"", "" );
        this.headers = columns ( line, AppConfig.properties.getCharSeparatorPoi ( ), 0 );
        getForeignColumnIndex ( );
        headers.add ( "cityName" );
        headers.add ( "townName" );
        return this.headers;
    }
    // Get foreign key column index in poi cvs file headers

    private void getForeignColumnIndex() {
        for (int i = 0; i < headers.size ( ); i++) {
            if (headers.get ( i ).equals ( "cityId" )) cityColumnIndex = i;
            else if (headers.get ( i ).equals ( "townId" )) townColumnIndex = i;
        }

    }
    // Convert Poi CVS line to elastic search columns for indexing

    @Override
    public List <String> linePoi() throws Exception {
        List <String> columns = line ( brPoi, AppConfig.properties.getCharSeparatorPoi ( ), 0 );
        addForeignColumns ( columns );
        return columns;

    }

    //Convert CVS line to elastic search columns for indexing
    private List <String> line(BufferedReader br, String seperator, int columnCount) throws Exception {
        String line = br.readLine ( );
        if (line == null) {
            br.close ( );
            return null;
        }
        line = line.replace ( "\"", "" );
        return columns ( line, seperator, columnCount );
    }

    private BufferedReader getFileContent(String filePath) throws UnsupportedEncodingException {
        try {
            return new BufferedReader ( new InputStreamReader ( new FileInputStream ( filePath ),
                    AppConfig.properties.getCharSet ( ) ) );
        } catch (FileNotFoundException e) {
            log.error (String.format ( "Invalid file path. File not found. File name: %s. \n" +
                    "Set correct poi,city or town file paths in /src/main/resources/default.properties file.",filePath));
            System.exit(0);
        }
        return null;
    }

}
