function download() {
    let downloadRequest = new XMLHttpRequest();

    let url = "http://localhost:8080/api/download?dbToken=" + dbToken;

    console.log(url);

    $.get(url, null);

    window.location.href = url;
}