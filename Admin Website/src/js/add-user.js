
// var admin = require("firebase-admin");
//
// //Gives administrator functionality for certain tasks
// var serviceAccount = require('roomdatabaseadder-firebase-adminsdk-j5izb-25151ad24f.json');
//
// admin.initializeApp({
// 	credential: admin.credential.cert(serviceAccount),
// 	databaseURL: 'https://roomdatabaseadder.firebaseio.com'
// });


//-------------------------------------------------------------------------------------------------------------------------------
// Get element in the DOM
const newUserSubmit = document.getElementById('NewUserSubmit');


var listOfUsers = [];

// Add New User event
NewUserSubmit.addEventListener('click', e => {
	//TODO Check if email already exists & validate email and password
	const email = document.getElementById("newUserEmail").value;
	const password = document.getElementById("newUserPassword").value;
	const passwordConfirm = document.getElementById("newUserPasswordConfirm").value;

	if (password != passwordConfirm) {
		alert("Error: Passwords do not match.");
	} else {
    console.log("Adding User");
		firebase.auth().createUserWithEmailAndPassword(email, password).catch(function (error) {
			// Handle Errors here.
			var errorCode = error.code;
			var errorMessage = error.message;
			// ...
		});
	}
});
