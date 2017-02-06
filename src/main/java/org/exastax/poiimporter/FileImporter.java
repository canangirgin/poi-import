package org.exastax.poiimporter;

import org.exastax.poiimporter.file.IFileManager;
import org.exastax.poiimporter.es.IESManager;

import javax.annotation.Resource;
import java.util.List;

public class FileImporter implements IFileImporter {

    @Resource
    private IFileManager fileManager;
    @Resource
    private IESManager   esManager;

    //load poi file file to elastic search with relations
    @Override
    public void process() throws Exception {
        List <String> headers = fileManager.getHeadersPoi ( );
        List <String> columns = null;
        esManager.createIndex ();
        while ((columns = fileManager.linePoi ( )) != null) {
            esManager.addBulk ( headers, columns );
        }
    }

}
