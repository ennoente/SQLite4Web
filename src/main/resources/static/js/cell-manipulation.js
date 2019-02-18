let originalText = '';
let alreadySelectedCell = null;
let currentInput;
let columnName;

// The columnNames JSONArray is already initialized after the successful DB upload
let columnValues = [

];

/**
 * Styles and adds functionality to the table.
 *
 * TODO: DOKU SCHREIBEN
 */
function addCellManipulation() {
  const td$ = $('#table:has(td)');

  td$.mouseover(function () {
    $(this).css('cursor', 'pointer');
  });

  td$.dblclick((e) => {
    // Remove the current input field, if existing
    currentInput = $('#input');
    currentInput.closest('td').text(currentInput.val());
    currentInput.remove();

    // If another cell was already selected set its text to its original cell
    // since its change was not submitted
    if (alreadySelectedCell != null) { alreadySelectedCell.text(originalText); }

    const clickedCell = $(e.target).closest('td');
    clickedCell.attr('id', 'clickedCellId');

    columnName = columnNames[clickedCell.index()];
    alreadySelectedCell = clickedCell;
    originalText = clickedCell.text();

    const $closestTr = alreadySelectedCell.closest('tr');

    // Clear the array from previous requests
    columnValues = [

    ];

    $closestTr.find('td').each(function () {
      console.log(`cell text=${$(this).text()}`);
      columnValues.push(
        $(this).text()
      );
    });


    console.log(originalText);
    console.log(`column name: ${columnName}`);

    const input = $('<input id="input" type="text" />');

    setFocusOnInput(input, () => {
      input.val(originalText);
      input.focus();
    });

    input.bind('enterKey', () => {
      updateCell();
    });

    input.bind('escapeKey', () => {
      removeInput();
    });

    input.keydown(function (e) {
      switch (e.keyCode) {
        case 13:
          $(this).trigger('enterKey');
          break;
        case 27:
          $(this).trigger('escapeKey');
      }
    });

    clickedCell.html(input);
  });
}

/**
 * Removes the input field and restores the old value of the cell.
 */
function removeInput() {
  const input$ = $('#input');
  const closestCell = input$.closest('td');
  input$.remove();
  closestCell.text(`${originalText}`);
}


let setFocusOnInput = function (selector, callback) {
  if (document.querySelector('#input') != null) {
    callback();
  } else {
    setTimeout(() => {
      setFocusOnInput(selector, callback);
    }, 8);
  }
};


function updateCell() {
  const newValue = $('#input').val();
  // Update local table -- UI
  $('#input').closest('td').text($('#input').val());
  alreadySelectedCell.text(`${newValue}`);

  // Send update request to server
  const url = '/api/update/cell';


  const request = new XMLHttpRequest();

  // TODO ALTER WERT SOLL INS JSON ARRAY EINGETRAGEN WERDEN

  const requestBody = {
    primaryKey,
    dbToken,
    columnName,
    tableName,
    newValue,

    columnNames,
    columnDataTypes,
    columnValues,
  };

  console.log(requestBody);

  request.open('POST', '/api/update/cell');

  request.onload = function () {
    console.log('Success! :)');
  };

  request.onerror = function () {
    console.log('oh-oh! :S');
  };


  $.ajax({
    url: '/api/update/cell',
    type: 'POST',
    contentType: 'application/json',
    dataType: 'json',
    data: JSON.stringify(requestBody),
    error(xhr, status, error) {
      console.log(`Error, status=${status}; error=${error}`);
      console.log(xhr.status);
    },
    success(data) {
      console.log('Success!');
      console.log(data);
    },
  });

  alreadySelectedCell = null;
}
