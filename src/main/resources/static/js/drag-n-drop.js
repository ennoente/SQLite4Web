function dragAndDropInit() {
    let $dropzone = document.getElementById("dropContainer");

    $dropzone.addEventListener('dragover', function (e) {
        e.stopPropagation();
        e.preventDefault();
        e.dataTransfer.dropEffect = 'copy';
    });

    $dropzone.addEventListener('drop', function (e) {
        e.stopPropagation();
        e.preventDefault();

        let databaseFile = e.dataTransfer.files[0];
        uploadFile(databaseFile);
    });
}

