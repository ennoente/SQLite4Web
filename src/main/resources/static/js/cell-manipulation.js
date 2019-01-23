let originalText = "";
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
    let td$ = $("#table:has(td)");

    td$.mouseover(function () {
        $(this).css("cursor", "pointer");
    });

    td$.dblclick(function (e) {
        // Remove the current input field, if existing
        currentInput = $("#input");
        currentInput.closest("td").text(currentInput.val());
        currentInput.remove();

        // For clarity
        //let newInput = $("#input");

        // If another cell was already selected set its text to its original cell
        // since its change was not submitted
        if (alreadySelectedCell != null)
            alreadySelectedCell.text(originalText);

        let clickedCell = $(e.target).closest("td");
        clickedCell.attr("id", "clickedCellId");

        columnName = columnNames[clickedCell.index()];
        alreadySelectedCell = clickedCell;
        originalText = clickedCell.text();

        // ...
        let $closestTr = alreadySelectedCell.closest('tr');

        $closestTr.find("td").each(function() {
            console.log("cell text=" + $(this).text());
            columnValues.push(
                $(this).text()
            );
        });
        // ...

        console.log(originalText);
        console.log("column name: " + columnName);

        let input = $('<input id="input" type="text" />');

        setFocusOnInput(input, function() {
            input.val(originalText);
            input.focus();
        });

        input.bind("enterKey", function(e) {
            updateCell();
        });

        input.bind("escapeKey", function(e) {
            removeInput();
        });

        input.keydown(function(e) {
            switch (e.keyCode) {
                case 13:
                    $(this).trigger("enterKey");
                    break;
                case 27:
                    $(this).trigger("escapeKey");
            }
        });

        clickedCell.html(input);
    });
}

/**
 * Removes the input field and restores the old value of the cell.
 */
function removeInput() {
    let input$ = $("#input");
    let closestCell =input$.closest("td");
    input$.remove();
    closestCell.text("" + originalText);
}


let setFocusOnInput = function(selector, callback) {
    if (document.querySelector("#input") != null) {
        callback();
    } else {
        setTimeout(function() {
            setFocusOnInput(selector, callback);
        }, 8);
    }
};


function updateCell() {
    let newValue = $("#input").val();
    // Update local table -- UI
    $("#input").closest("td").text($("#input").val());
    //alreadySelectedCell.text($("#input").val());
    alreadySelectedCell.text("" + newValue);

    // Send update request to server
    let url = "http://localhost:8080/api/update/cell";


    let request = new XMLHttpRequest();

    // TODO ALTER WERT SOLL INS JSON ARRAY EINGETRAGEN WERDEN

    let requestBody = {
        "primaryKey": primaryKey,
        "dbToken": dbToken,
        "columnName": columnName,
        "tableName": tableName,
        "newValue": newValue,

        "columnNames": columnNames,
        "columnDataTypes": columnDataTypes,
        "columnValues": columnValues
    };

    console.log(requestBody);

    //if (primaryKey != null) {
    //    url += "?primaryKey=" + primaryKey;
    //}

    request.open("POST", "http://localhost:8080/api/update/cell");

    request.onload = function() {
        console.log("Success! :)");
    };

    request.onerror = function() {
        console.log("oh-oh! :S");
    };

    let _requestbody = JSON.stringify(requestBody);

    console.log("REQUEST BODY: " + _requestbody);


    $.ajax({
        url: 'http://localhost:8080/api/update/cell',
        type: 'POST',
        contentType: 'application/json',
        data: _requestbody,
        dataType: 'json',
        error: function (xhr, status, error) {
            console.log("Error, status=" + status + "; error=" + error);
            console.log(xhr.status);
        },
        success: function (data) {
            console.log("Success!");
            console.log(data);
        }
    });

    /*
    request.send(JSON.stringify(requestBody));
    */


    alreadySelectedCell = null;
}










