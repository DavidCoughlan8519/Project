

function listAllUsers(nextPageToken) {
	// List batch of users, 1000 at a time.
	admin.auth().listUsers(1000, nextPageToken)
		.then(function (listUsersResult) {
			listUsersResult.users.forEach(function (userRecord) {
				console.log("user", userRecord.toJSON());
        //listOfUsers.push(userRecord.);
			});
			if (listUsersResult.pageToken) {
				// List next batch of users.
				listAllUsers(listUsersResult.pageToken)
			}
		})
		.catch(function (error) {
			console.log("Error listing users:", error);
		});
}

//Clicked on the button to launch the Deltee User Modal
deleteUserModalBtn.addEventListener('click', e => {
// Start listing users from the beginning, 1000 at a time.
//empty the list first
listOfUsers = [];
//populate the list
// If no pageToken is specified, the operation will list users from the beginning, ordered by creation time.
listAllUsers();
console.log("Ran");

    var select = document.getElementById("adminSelector");
    for(var i = 0; i < listOfUsers.length; i++) {
      var opt = listUsers[i];
      var el = document.createElement("option");
      el.textContent = opt;
      el.value = opt;
      select.appendChild(el);
    }
});

function deleteAdmin(uid) {
	admin.auth().deleteUser(uid)
		.then(function () {
			console.log("Successfully deleted user");
      alert("Successfully deleted user");
		})
		.catch(function (error) {
			console.log("Error deleting user:", error);
      alert("Error deleting user:",error);
		});
}
