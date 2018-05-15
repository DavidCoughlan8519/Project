

function ValidateEmail(inputText) {
	var mailformat = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
	if (inputText.value.match(mailformat)) {
		document.form1.text1.focus();
		return true;
	} else {
		alert("You have entered an invalid email address!");
		document.form1.text1.focus();
		return false;
	}
}

// Get element in the DOM
const loginbtn = document.getElementById('loginSubmit');

// Add login event
loginbtn.addEventListener('click', e => {
	// Get values of elements in the DOM
	const email = document.getElementById("email").value;
	const password = document.getElementById("password").value;
	// Sign in
	firebase.auth().signInWithEmailAndPassword(email, password).catch(function(error) {
			// Handle Errors here.
			var errorCode = error;
			var errorMessage = errorCode.message;
			if (errorCode.code === 'auth/wrong-password') {
				alert('Wrong password.');
			} else {
				alert(errorMessage);
			}
			console.log(error);
		});
				return false;
});

firebase.auth().onAuthStateChanged(function (user) {
	if (user) {
		//After successful login, user will be redirected to admin.html
		window.location = "admin.html";
	} else {
		console.log("Not logged in.");
	}
});
