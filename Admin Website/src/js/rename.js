const renameButton = document.getElementById('renameButton');
const saveRenameButton = document.getElementById('saveRenameButton');
var coordinates = [];

//Update the locations names
renameButton.addEventListener('click', e => {
	if(selectedLocation != null){
	document.getElementById("selectedRenameRoom").innerHTML = selectedLocation[1];
}else{document.getElementById("selectedRenameRoom").innerHTML = "Please select a location first."}
});


saveRenameButton.addEventListener('click', e => {
	//get the name typed by the user
	var newName = document.getElementById('NewLocationName').value;

	//Checks to see if they supplied a new name for the location
	if (newName != null) {

		var floorLocationsRef = firebase.database().ref('locations/' + selectedLocation[1].charAt(0) + ' Block/' + selectedLocation[1].charAt(1) + ' Floor/');
		floorLocationsRef.once('value').then(function (snap) {
			snap.forEach(function (floorLocations) {

				if (floorLocations.key == selectedLocation[1]) {
					//Save coordinates
					var locationData = floorLocations.val();
					console.log("LocationData: " + locationData);
					coordinates = Object.values(locationData);

					for (i = 0; i < coordinates.length; i++) {
	    			console.log(i + "		" + coordinates[i]);
					}

					var oldLocation = firebase.database().ref('locations/' + selectedLocation[1].charAt(0) + ' Block/' + selectedLocation[1].charAt(1) + ' Floor/' + selectedLocation[1]);

					//Delete the entry
					oldLocation.remove();
					//Write the entry
					var blockRef = firebase.database().ref('locations/' + newName.charAt(0) + ' Block');
					var floorRef = blockRef.child(newName.charAt(1) + ' Floor');
					var newLocationRef = floorRef.child(newName);
					//TODO: Get the values saved in locationData and populate the paths below with values
					console.log("Longitude: " + coordinates[0]);
					console.log("Latitude: " + coordinates[1]);
					console.log("Date: " + coordinates[2]);

					newLocationRef.child("Longitude").set(coordinates[0]);
					newLocationRef.child("Latitude").set(coordinates[1]);
					newLocationRef.child("Date").set(coordinates[2]);
					selectedRow.getElementsByTagName("td")[1].innerHTML = newName;
					selectedLocation[1] = newName;
				}
			});

			//return floorLocations.update(data);
			return null;
		});

		//If they did not supply a new name tell them through an alert.
	} else {
		alert('New location name cannot be empty.');
	}

});
