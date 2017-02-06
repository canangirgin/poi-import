package org.exastax.poiimporter.es;

import java.util.List;

public interface IESManager {

    public void addBulk(List <String> headers, List <String> values) throws Exception;

    public void createIndex() throws Exception;
}
