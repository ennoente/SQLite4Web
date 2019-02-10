let primaryKey;
let dbToken;
let columnNames;
let tableName;
let columnDataTypes;

// Binds the @uploadFile function to file input's onChange-Event
$(document).ready(() => {
  $('#hiddenInput').on('change', () => {
    uploadFile($('#hiddenInput')[0].files[0]);
  });
});

/**
 * Uploads the selected file to the REST API via AJAX's XMLHttpRequest.
 * @param file The file to be sent to the server
 */
function uploadFile(file) {
  // Create FormData object carrying the file
  const formData = new FormData();

  formData.append('file', file);

  const uploadRequest = new XMLHttpRequest();

  // Success callback
  uploadRequest.onload = function () {
    // Store the JSON response into variable
    const uploadResponse = JSON.parse(this.responseText);

    console.log('Server response: ');
    console.log(uploadResponse);
    console.log(`dbToken=${uploadResponse.dbToken}`);

    window.location.replace(`/?dbToken=${uploadResponse.dbToken}`);
  };

  // Error callback
  uploadRequest.onerror = function () {
    alert('Oh neeeej!');
  };

  // Open connection and send
  uploadRequest.open('POST', '/api/upload');
  uploadRequest.send(formData);
}

function retrieveResponseAndBuildTable(uploadResponse) {
  // Parse response JSON
  const data = uploadResponse.data;
  const dataLength = uploadResponse.data.length;
  const $table = $('#table');
  columnNames = uploadResponse.metadata.columnNames;
  columnDataTypes = uploadResponse.metadata.columnDataTypes;
  primaryKey = uploadResponse.metadata.primaryKey;
  dbToken = uploadResponse.metadata.dbToken;
  tableName = uploadResponse.metadata.tableName;

  // Logging
  console.log(uploadResponse);
  console.log('metadata: ');
  console.log(columnNames);
  console.log(`Data length: ${dataLength}`);

  constructTableFromResponse($table, columnNames, data, dataLength);
  addCellManipulation();
}
