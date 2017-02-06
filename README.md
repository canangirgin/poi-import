# CSV to Elasticsearch Poi importer
Project imports poi data from cvs file to elastic search index.
Before starting importer you must set elastic search cluster parameters and cvs file parameters in properties file.


## File Formats:

**Poi file**

First row must contains headers.
Least columns poi file must contains:  
~~~~
"id","latitude","longitude","townId","cityId"
~~~~
poi file sample 
~~~~

"id","name","category","topCategory","zipCode","oldQuarters","oldTowns","latitude","longitude","streetId","quarterId","townId","cityId"
"40200000399","Kent Eczanesi","Eczane","Eczane","40200","","","39.14404","34.16026","","50015","1472","40"
~~~~

**City file**

First row must contains headers.
Least columns poi file must contains:  
~~~~
"cityid","cityname"
~~~~
city file sample 
~~~~
cityid;cityname;wkt
1;ADANA;
~~~~

**Town file**

First row must contains headers.
Least columns poi file must contains:  
~~~~
"townid","townname"
~~~~
town file sample 
~~~~
townid;townname;wkt
1101;ABANA;
~~~~

##  RUN
~~~~
org.exastax.poiimporter.MainProcess 
~~~~
**Run for debug or development**
you can change development.properties file and run :
~~~~
org.exastax.poiimporter.MainProcess -Dspring.profiles.active=development
~~~~