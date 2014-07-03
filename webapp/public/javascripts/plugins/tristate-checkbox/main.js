/**
 * Found on: https://github.com/supernifty/tristate-checkbox
 * Edited by Hannes Mareen
 */
var supernifty_tristate = function() {
  var
    YES = { image: yesImage, state: "1" },
    NO = { image: noImage, state: "0" },
    NONE = { image: noneImage, state: "-1" };

  function tristate_elements() {
    if ( document.getElementsByClassName != undefined ) {
      return document.getElementsByClassName( "tristate" );
    }
    else {
      var 
        all = document.getElementsByTagName('*'),
        alllength = all.length,
        result = [], i;
      for ( i = 0; i < alllength; i++ ) {
        if ( all[i].className == 'tristate' ) {
          result.push( all[i] );
        }
      }
      return result;
    }
  }

  return {
    init: function() {
      var list = tristate_elements(), 
        i, 
        html;
      for ( i = 0; i < list.length; i++ ) {
        html = "<img id=\"" + list[i].id + "_img\" src=\"" + NONE.image + "\" onclick=\"supernifty_tristate.update('" + list[i].id + "')\"/><input class=\"searchTextField\" type=\"hidden\" id=\"" + list[i].id + "_frm\" name=\"" + list[i].id + "\" value=\"" + NONE.state + "\"/>";
        list[i].innerHTML = html;
      }
    },

    update: function(id) {
      var state = document.getElementById( id + "_frm" ).value, next;
      // yes -> no -> none -> yes
      if ( state == '1' ) {
        next = NO;
      }
      else if ( state == '0' ) {
        next = NONE;
      }
      else { // assume none
        next = YES;
      }
      document.getElementById( id + "_img" ).src = next.image;
      document.getElementById( id + "_frm" ).value = next.state;
    }
  }
}();

// onload handler
var existing_onload = window.onload;
window.onload = function() {
  if ( existing_onload != undefined ) {
    existing_onload();
  }
  supernifty_tristate.init();
}

$(document).ready(supernifty_tristate.init());
