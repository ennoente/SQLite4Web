function constructTableFromResponse(table$, metadata, data, dataLength) {
  // Construct table header row
  const headerRow$ = $('<tr/>');
  for (let i = 0; i < metadata.length; i++) {
    const $header = $('<th/>');
    $header.html(metadata[i]);
    $header.addClass(i);

    headerRow$.append($header);
  }
  table$.append(headerRow$);

  // Construct table data rows
  for (let i = 0; i < dataLength; i++) {
    const row$ = $('<tr/>');
    const currentRow = data[i];
    for (let j = 0; j < currentRow.length; j++) {
      const $td = $('<td/>');
      $td.html(currentRow[j]);
      $td.addClass(i);

      row$.append($td);
    }
    table$.append(row$);
  }
}
