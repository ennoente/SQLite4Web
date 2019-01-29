function download() {
    let downloadRequest = new XMLHttpRequest();

    let url = "/api/download?dbToken=" + dbToken;

    console.log(url);

    $.get(url, null);

    window.location.href = url;
}
