/*
 * =========================================
 * Datetimeinput v2.1
 * =========================================
 *
 * This script contains an extra plugin to JQuery named datetimeinput
 * giving an input field several extra functionalities:
 * - input validation of a date, time or datetime
 * - restriction of input tokens to numbers only
 *
 * An datetimeinput can be creating whilst handing it several specific options. Though these
 * options are not yet implemented, the functionality is already added to the datetimeinput object.
 *
 * Created by Benjamin on 27/03/2014.
 */
!function ($) {

    /**
     * Create the datetimeinput object.
     * @constructor
     */
    var Datetimeinput = function (element, options) {
        this.element = $(element);
        this.formatString = options.formatString || "yyyy-mm-dd hh:ii";

        this.startNode = null;
        this.yearNode = null;
        this.monthNode = null;
        this.dayNode = null;
        this.hourNode = null;
        this.minuteNode = null;
        this.currentNode = null;

        this.fieldJumpPerformed = false;
        this._initNodes(this.formatString);
        this._attachEvents();

        if(this.element.val() === '')
            this.element.val(this.formatString);
        else
            this._fillInNodes(this.element.val());
    };

    // Date time prototype
    Datetimeinput.prototype = {
        constructor: Datetimeinput,
        _events: [],

        /**
         * Private function attaching several events to the element containing the
         * datettimeinput object
         * @private
         */
        _attachEvents: function () {
            this._detachEvents();
            this._events = [
                [this.element, {
                    click: $.proxy(this._click, this),
                    focusin: $.proxy(this._focusin, this),
                    focusout: $.proxy(this._focusout, this),
                    keydown: $.proxy(this._keydown, this),
                    keypress: $.proxy(this._keypress, this),
                    mouseover: $.proxy(this._mouseover, this)
                }]
            ];
            for (var i = 0, el, ev; i < this._events.length; i++) {
                el = this._events[i][0];
                ev = this._events[i][1];
                el.on(ev);
            }
        },

        /**
         * Detach events attached to the element containing this datetimeinput
         * @private
         */
        _detachEvents: function () {
            for (var i = 0, el, ev; i < this._events.length; i++) {
                el = this._events[i][0];
                ev = this._events[i][1];
                el.off(ev);
            }
            this._events = [];
        },

        /**
         * Function responsible for initializing the nodes representing year, month
         * day, hour and minutes.
         * The nodes create a linkedList according to the formatted string provided
         * to the function. This allows easy transversal.
         * @param string The formatted string
         * @private
         */
        _initNodes: function (string) {
            var previousNode = null;
            for (var i = 0; i < string.length; i++) {
                var token = string.charAt(i);
                var start = i++;
                while (token == string.charAt(i)) {
                    i++;
                }
                var node = new LLNode(start, i, token);
                if (previousNode == null)
                    this.startNode = node;
                else
                    previousNode.next = node;
                node.previous = previousNode;
                switch (token) {
                    case 'y':
                        this.yearNode = node;
                        break;
                    case 'm':
                        this.monthNode = node;
                        break;
                    case 'd':
                        this.dayNode = node;
                        break;
                    case 'h':
                        this.hourNode = node;
                        break;
                    case 'i':
                        this.minuteNode = node;
                        break;
                    default:
                        break;
                }
                previousNode = node;
            }
        },

        /**
         * Function responsible parse a provided value according to the formatted
         * string contained in this datetimeinput object and fill in the nodes
         * with the correct values.
         * @param string The string provided
         * @private
         */
        _fillInNodes: function (string) {
            var node = this.startNode;
            while (node != null) {
                var val = parseInt(string.substring(node.start, node.end));
                if(!isNaN(val))
                    node.value = val;
                node = node.next;
            }
        },

        // EVENT HANDLERS

        /**
         * Function triggered after a click event.
         * The currentNode is initialised after a check whether the value of the
         * previous currentNode is validate. If not, the previous currentNode
         * remains the currentNode.
         * The function _toFormattedString is called.
         * @private
         */
        _click: function () {
            this._fillInNodes(this.element.val());
            if (this.currentNode == null || this._validateValue()) {
                this.position = this.element[0].selectionStart - 1;
                if (this.position == -1)
                    this.position = 1;
                this.currentNode = this.startNode;
                while (this.currentNode != null && this.position > this.currentNode.end)
                    this.currentNode = this.currentNode.next;
                this.position = this.currentNode.end;
            }
            this.fieldJumpPerformed = false;
            this._toFormattedString();
        },

        /**
         * Function triggered after a mouseover event.
         * Force the cursor to the default cursor.
         * @param evt the event
         * @private
         */
        _mouseover: function () {
            this.element.css('cursor', 'default');
        },

        /**
         * Function triggered after a focusin event.
         * Makes a call to the _fillInNodes method with the value currently
         * stored in the inputfield.
         * @private
         */
        _focusin: function () {
            if(this.currentNode == null)
                this.currentNode = this.startNode;
            this._fillInNodes(this.element.val());
        },

        /**
         * Function triggered after a focusout event.
         * Allows the lose of focus, if and only if, the provided value
         * (if any) of the current node is valid.
         * @private
         */
        _focusout: function () {
            this._fillInNodes(this.element.val());
            if (this.currentNode != null && !this._validateValue())
                this.element.focus();
            else
                this.currentNode = null;
            this._toFormattedString();
        },

        /**
         * Function triggered after a keydown event.
         * Handles specific keys (e.g. arrow keys, backspace, delete,...)
         * @param evt The event
         * @private
         */
        _keydown: function(evt) {
            var code = evt.which;
            if(code == 37 || code == 40) {
                if(this.currentNode.value == 0)
                    this.currentNode.value = 1;
                this.currentNode = (this.currentNode.previous != null) ? this.currentNode.previous : this.currentNode;
                this.position = this.currentNode.end;
            }
            else if(code == 39 || code == 38) {
                if(this.currentNode.value == 0)
                    this.currentNode.value = 1;
                this.currentNode = (this.currentNode.next != null) ? this.currentNode.next : this.currentNode;
                this.position = this.currentNode.end;
            }
            else if(code == 8 || code == 46) {
                if (this.position < this.currentNode.end) {
                    this.currentNode.value = Math.floor(this.currentNode.value / 10);
                    this.position++;
                }
            } else
                return;
            evt.preventDefault();
            this._toFormattedString();
        },

        /**
         * Function triggered after a keypress event.
         * The default action triggered by this event is prevented.
         * If the currentNode is defined, the value entered is parsed and
         * validated.
         * @param evt The event containing the key pressed
         * @private
         */
        _keypress: function (evt) {
            evt.preventDefault();
            if (this.currentNode == null)
                return;
            if (this.position == this.currentNode.end)
                this.currentNode.value = 0;
            var num = parseInt(String.fromCharCode(evt.which));
            if (!isNaN(num)) {
                if (this._validateValue(num)) {
                    if (!this.fieldJumpPerformed) {
                        this.position--;
                        if (this.position == this.currentNode.start) {
                            this._fieldJump()
                        }
                    }
                    this.fieldJumpPerformed = false;
                }
            }
            this._toFormattedString();
        },

        /**
         * Private function responsible for the representation of the
         * values stored in the nodes, according to the formatted string.
         * @private
         */
        _toFormattedString: function () {
            var node = this.startNode;
            var string = '';
            while (node != null) {
                var valstr = '';
                if (this.currentNode != null && this.currentNode == node && this.position < this.currentNode.end) {
                    var diff = this.position - node.start;
                    while (diff > 0) {
                        valstr += ' ';
                        diff--;
                    }
                }
                if (node.value != -1) {
                    while ((valstr + node.value).length < node.end - node.start)
                        valstr += '0';
                    valstr += node.value;
                    string += valstr + ((node.next != null) ? this.formatString.substring(node.end, node.next.start) :
                        this.formatString.substring(node.end));
                } else {
                    var end = (node.next != null) ? node.next.start : node.end;
                    string += this.formatString.substring(node.start, end);
                }
                node = node.next;
            }
            this.element.val(string);
            if (this.currentNode != null) {
                window.getSelection().removeAllRanges();
                this.element[0].setSelectionRange(this.currentNode.start, this.currentNode.end);
            }
        },

        /**
         * Private function called when a field jumped is being performed.
         * A field jump, is the jump to next node in the linkedlist.
         * @private
         */
        _fieldJump: function () {
            if (this.currentNode.next != null)
                this.currentNode = this.currentNode.next;
            this.position = this.currentNode.end;
        },

        /**
         * Private function called to perform a forced field jump.
         * @private
         */
        _forceFieldJump: function () {
            this._fieldJump();
            this.fieldJumpPerformed = true;
        },

        /**
         * Private function capable of detecting whether the current node is
         * the year-, month-, day-, hour- or minutesnode and performing a call to
         * the correct method to validate the value provided to the current node.
         * @param value The value provided to the current node (this value is appended to the node)
         * @returns {boolean} True if the resulting value is valid, false otherwise
         * @private
         */
        _validateValue: function (value) {
            var result = false;
            var backup = this.currentNode.value;
            if (!(typeof value === 'undefined'))
                this.currentNode.appendValue(value);
            switch (this.currentNode.token) {
                case 'y':
                    result = this._validateYear();
                    this._correctDate();
                    break;
                case 'm':
                    result = this._validateMonth();
                    this._correctDate();
                    break;
                case 'd':
                    result = this._validateDay();
                    this._correctDate();
                    break;
                case 'h':
                    result = this._validateHour();
                    break;
                case 'i':
                    result = this._validateMinutes();
                    break;
            }
            if (!result)
                this.currentNode.value = backup;
            return result;
        },

        /**
         * @returns {boolean} True whenever the value contained in the yearnode is valid.
         * @private
         */
        _validateYear: function () {
            return true;
        },

        /**
         * @returns {boolean} True whenever the value contained in the monthnode is valid.
         * @private
         */
        _validateMonth: function () {
            return this._validate(12, false);
        },

        /**
         * @returns {boolean} True whenever the value contained in the daynode is valid.
         * @private
         */
        _validateDay: function () {
            return this._validate(31, false);
        },

        /**
         * @returns {boolean} True whenever the value contained in the hournode is valid.
         * @private
         */
        _validateHour: function () {
            return this._validate(23, true);
        },

        /**
         * @returns {boolean} True whenever the value contained in the minutnode is valid.
         * @private
         */
        _validateMinutes: function () {
            return this._validate(59, true);
        },

        /**
         * @returns {boolean} True whenever the value contained in the currentNode is valid
         * according to the maximum value provided and determining whether zero is a valid.
         * @param max The maximum value allowed
         * @param allowZero True is zero is allowed as value
         * @returns {boolean} True whenever the value contained in the current node is valid.
         * @private
         */
        _validate: function (max, allowZero) {
            var dec = Math.floor(max / 10);
            if (this.currentNode.value > dec && this.currentNode.value < (max + 1)) {
                this._forceFieldJump();
            }
            else if (this.currentNode.value > (max) || (!allowZero && this._isLastToken() && this.currentNode.value == 0))
                return false;
            return true;
        },

        /**
         * Correct the day contained in the daynode according to the year and month contained
         * in the corresponding nodes.
         * @private
         */
        _correctDate: function () {
            var max = DTIGlobal.getDaysInMonth(this.monthNode.value - 1, this.yearNode.value);
            if (this.dayNode.value > max)
                this.dayNode.value = max;
        },

        /**
         * @returns {boolean} True if the token provided is the last token for this field (node).
         * @private
         */
        _isLastToken: function () {
            return this.position == (this.currentNode.start + 1);
        },

        // EXTRA FUNCTIONS
        /**
         * Provide a value to this datetimeinput. The value will be processed
         * and stored in the correct nodes according to the formatted string.
         * @param value The value provided
         * @param format The format provided
         * @private
         */
        provideValue: function (value, format) {
            if (format === this.formatString)
                this._fillInNodes(value);
            this._toFormattedString();
        }
    };

    /**
     *  Add the datetimeinput to Jquery to be available.
     */
    $.fn.datetimeinput = function(option) {
        var args = Array.apply(null, arguments);
        args.shift();
        return this.each(function () {
            var $this = $(this),
                data = $this.data('datetimeinput'),
                options = typeof option == 'object' && option;
            // Initialise the datetimeinput if it not yet exists
            if (!data) {
                $this.data('datetimeinput', (data = new Datetimeinput(this, $.extend({}, options))));
            }
            // Execute a function call
            if (typeof option == 'string' && typeof data[option] == 'function') {
                data[option].apply(data, args);
            }
        });
    };

    $.fn.datetimeinput.Constructor = Datetimeinput;

    /**
     * Global functions of the datetimeinput plugin
     */
    var DTIGlobal = {
        isLeapYear: function(year) {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
        },

        getDaysInMonth: function(month, year) {
            return [31, (DTIGlobal.isLeapYear(year) ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
        }
    };

    /**
     * Linkedlist node containing:
     * - the start index if the node
     * - the end index
     * - the value contained in this node,
     * - the token identifying this node
     * - the linkedlist node following this node
     * @constructor
     */
    var LLNode = function(start, end, token) {
        this.start = start;
        this.end = end;
        this.value = -1;
        this.token = token;
        this.next = null;
        this.previous = null
    };

    LLNode.prototype = {
        constructor: LLNode,

        /**
         * @param value Append the value to the current value contained in the node
         */
        appendValue: function(value) {
            this.value  = this.value * 10 + value;
        }
    };


}(window.jQuery);