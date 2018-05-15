var categories = 0; //Number of groupings of locations
var locations = 0; // Number of logged locations in the database
var content = ""; // List of all the rooms names logged in the database
var floorLevel = "";
var date = "";
var tableData = [];
var table;
var selectedLocation = [];
var selectedRow = null;


function loadTable() {
	//clears the table
	$('#myTableBody').empty();
	//Gets the data for the table
	var ref = firebase.database().ref("locations/");
	ref.on("value", function (snapshot) {
		snapshot.forEach(function (block) {
			//count the number of categories
			if (block.key !== null) {
				categories++;
			}

			block.forEach(function (floor) {
				floorLevel = floor.key;
				floor.forEach(function (room) {
					if (room.exists()) {
						locations++;
						var roomName = room.key;
						room.forEach(function (date){
							if(date.key === "Date")
							{
								date = date.val();
								var data = [locations, roomName, floorLevel, date];
								tableData.push(data);
							}
						});
					}
				});
			});
		});


		//Makes the table when the docuement is laoded and writes to the dom.
		$(document).ready(function () {
			//Clear the table before writing to it
			table = $('#room-table').DataTable({
				data: tableData,
				columns: [{
						title: "Number"
					},
					{
						title: "Name"
					},
					{
						title: "Floor"
					},
					{
						title: "Date"
					}
				],
				select: true,
				"bRetrieve": true,
				"columnDefs": [{
					"className": "dt-center",
					"targets": "_all"
				}]
			});

			// For the selected row in the table
			$('#myTableBody').on('click', 'tr', function () {
				if ($(this).hasClass('selected')) {
					$(this).removeClass('selected');
				} else {
					table.$('tr.selected').removeClass('selected');
					$(this).addClass('selected');
				}
			});
		});

		// Gets the data from the selected row and saves it in a variable
		$('#room-table').on('click', 'tr', function () {
			selectedLocation = table.row(this).data();
			selectedRow = this;
		});

		//sets the tile for categories with an updated value
		document.getElementById("categories-count").innerHTML = categories;
		//sets the tile for locations with an updated value
		document.getElementById("locations-count").innerHTML = locations;
	});
}


loadTable();
