function dragAndDropInit() {
  const $dropzone = document.getElementById('dropContainer');

  $dropzone.addEventListener('dragover', (e) => {
    e.stopPropagation();
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy';
  });

  $dropzone.addEventListener('drop', (e) => {
    e.stopPropagation();
    e.preventDefault();

    const databaseFile = e.dataTransfer.files[0];
    uploadFile(databaseFile);
  });
}