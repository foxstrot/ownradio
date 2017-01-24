var db;
var dbName;

var alterDB = function(){
    if(db === undefined)
        throw new Error("Can't alter data base");

    db.transaction(function (tx) {
        tx.executeSql('CREATE TABLE IF NOT EXISTS TRACKS (id unique, name)');

        tx.executeSql('INSERT OR IGNORE INTO TRACKS (id, name) VALUES (1, "track 1")');
        tx.executeSql('INSERT OR IGNORE INTO TRACKS (id, name) VALUES (2, "track 2")');
        tx.executeSql('INSERT OR IGNORE INTO TRACKS (id, name) VALUES (3, "track 3")');
        tx.executeSql('INSERT OR IGNORE INTO TRACKS (id, name) VALUES (4, "track 4")');
        tx.executeSql('INSERT OR IGNORE INTO TRACKS (id, name) VALUES (5, "track 5")');
        tx.executeSql('INSERT OR IGNORE INTO TRACKS (id, name) VALUES (6, "track 6")');

    });
}

function openDB(){
  /*  if(window.sqlitePlugin !== undefined)
    {       
         db = window.sqlitePlugin.openDatabase({name: dbName, location: 'default'});        
    }
    else{*/
         db = window.openDatabase(dbName, '1.0', dbName, 2 * 1024 * 1024);             
   // }
}

var dataProvider ={
     initialize: function (config, successCallback, errorCallback) {
         if(config.db === undefined)
            throw new Error("Can't initialize data base");

        var dbName = config.db;

        openDB();
        alterDB();

     },

     tables: {
         tracks: 'TRACKS'
     },

     select: function(table, callback){

   //     openDB();

         db.transaction(function (tx) {
            tx.executeSql('SELECT * FROM '+table, [], function (tx, results) {
                callback(results.rows);             
            }, function(error){
                console.log('SELECT error: ' + error.message);
            });

            });
     }

}
