function download() {
  const downloadRequest = new XMLHttpRequest();

  const url = `/api/download?dbToken=${dbToken}`;

  console.log(url);

  $.get(url, null);

  window.location.href = url;
}
