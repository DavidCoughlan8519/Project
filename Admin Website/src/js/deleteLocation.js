

const deleteLocationButton = document.getElementById('deleteLocationButton');

//deleteLocationButton the locations name
deleteLocationButton.addEventListener('click', e => {

	if(selectedLocation != null){
		document.getElementById("selectedDeleteRoom").innerHTML = selectedLocation[1];
}else{document.getElementById("selectedDeleteRoom").innerHTML ="Please select a location first."}
});

const confirmLocationDeleteButton = document.getElementById('confirmDeletelocationButton');

confirmLocationDeleteButton.addEventListener('click', e => {

	var floorLocationsRef = firebase.database().ref('locations/' + selectedLocation[1].charAt(0) + ' Block/' + selectedLocation[1].charAt(1) + ' Floor/');
	floorLocationsRef.once('value').then(function (snap) {
		snap.forEach(function (floorLocations) {

			if (floorLocations.key == selectedLocation[1]) {
					floorLocationsRef.child(selectedLocation[1]).remove();
					alert("Success: Removed " + selectedLocation[1] + ".");
					//refresh the table after
					location.reload();
				  loadTable();
			}
		});
	});
});
