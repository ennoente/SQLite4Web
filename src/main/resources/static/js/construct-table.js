function constructTableFromResponse(table$, metadata, data, dataLength) {
    // Construct table header row
    let headerRow$ = $('<tr/>');
    for (let i = 0; i < metadata.length; i++) {
        let $header = $('<th/>');
        $header.html(metadata[i]);
        $header.addClass(i);

        headerRow$.append($header);

        //headerRow$.append($('<th/>').html(metadata[i]));
    }
    table$.append(headerRow$);

    // Construct table data rows
    for (let i = 0; i < dataLength; i++) {
        let row$ = $('<tr/>');
        let currentRow = data[i];
        for (let j = 0; j < currentRow.length; j++) {
            let $td = $('<td/>');
            $td.html(currentRow[j]);
            $td.addClass(i);

            row$.append($td);

            //row$.append($('<td/>').html(currentRow[j]));
        }
        table$.append(row$);
    }
}