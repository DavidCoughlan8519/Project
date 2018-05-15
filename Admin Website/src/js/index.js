var admin = require("firebase-admin");

//Gives administrator functionality for certain tasks

var serviceAccount = require('./college-pal-734fc-firebase-adminsdk-kun8j-085dd0b90b.json');

admin.initializeApp({
	credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://college-pal-734fc.firebaseio.com"
});
