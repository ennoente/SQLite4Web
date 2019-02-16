let nightMode = false;

$(document).ready(function () {
  const body$ = $('body');
  $(this).keydown((e) => {
    console.log(e.keyCode);
    if (e.keyCode === 78 && $('#input').length === 0) { // N -- only work if no input is present
      if (!nightMode) {
        body$.css('background-color', '#323232');
      } else {
        body$.css('background-color', '#FFFFFF');
      }

      nightMode = !nightMode;
    }
  });
});
