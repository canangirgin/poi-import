package org.exastax.poiimporter.file;

import java.util.List;

public interface IFileManager {

    public List <String> getHeadersPoi() throws Exception;

    public List <String> linePoi() throws Exception;

}
