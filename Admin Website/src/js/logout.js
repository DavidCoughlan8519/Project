// Initialize Firebase
var config = {
	apiKey: "AIzaSyDrsSEwop5ovYkIN4afTef1ulBKDjNYCA8",
	authDomain: "college-pal-734fc.firebaseapp.com",
	databaseURL: "https://college-pal-734fc.firebaseio.com",
	projectId: "college-pal-734fc",
	storageBucket: "college-pal-734fc.appspot.com",
	messagingSenderId: "311922210046"
};
firebase.initializeApp(config);

// Get element in the DOM
const logout = document.getElementById('logout');
// Add login event
logout.addEventListener('click', e => {
//Sign out
firebase.auth().signOut();
});

firebase.auth().onAuthStateChanged(function (user) {
	if (user) {
		console.log("Logged in: " + user.email);
    //Sets name for profile in nav-bar
    document.getElementById("nav-bar-name").innerHTML = user.email;
	} else {
    //After successful logging out user will be redirected to index.html
		console.log("Not logged in.");
		window.location = "index.html";
	}
});
