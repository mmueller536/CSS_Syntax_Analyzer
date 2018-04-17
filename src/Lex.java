import java.io.*;
import java.util.*;

public class Lex {
	//File Readers
	File testFile; //File that will be syntax checked and analyzed
	FileReader reader; // The initial reader that will gather info from file
	BufferedReader grabber; // The buffered reader that simply buffers the reader for the program
	
	//Array Parts
	int pos; //position in current lexeme
	int c; // character that is grabbed by the grabber
	char[] lexeme;// Current lexeme that is being analyzed
	
	//Debug Variables
	int line_pos;//translates to a column in a text editor
	int line_num;//translates to a row in a text editor

	String report;//The lexeme after being converted to a string
	String old_value = "null";//Used in background-position to save previous value
	
	//Utility and return variables
	int charClass;//The class of instructions for the next lexeme
	char nextChar;//c after being converted to a character.
	int nextToken;//the token of the lexeme post analysis
	
	//Token Codes
	final int CLASS_S = 1;//Class Selector Type
	final int ID_S = 2;//Id Selector Types
	final int ELEMENT_S = 3;//Element Selector Types
	final int VALID_STATE = 4;//A valid state that an element selector can take.
	                          //Represents a range of keywords that can be valid
	                          //states.
	final int ATTRIB_OP = 5;//token for a attribute operator
	final int SELECTOR_OP = 6;//token for a selector operator
	final int COLON = 7;//':' token
	final int STRING = 8;//string with quotes
	final int STRING_UNBOUND = 9;//string without quotes
	final int NUMBER = 10;//any number
	final int URL = 11;
	final int VALID_KEYWORD = 12;
	final int DIM_VALUE = 13;
	final int RECT = 14;
		
	//Special Char Codes (30)
	final int SEMICOL = 30;//semicolon token
	final int LEFT_BRACE = 31;//left brace token
	final int RIGHT_BRACE = 32;//right brace token
	final int LEFT_BRACKET = 33;//left bracket token
	final int RIGHT_BRACKET = 34;//right bracket token
	final int RIGHT_PAREN = 35;
	final int COMMA = 36;//Comma token
	
	//Color Tokens
	final int COLOR_NAME = 40;
	final int RGB_COLOR = 41;
	final int HEX_COLOR = 42;
	
	//Declaration Tokens
	final int COLOR_DEC = 50;
	final int BACKGROUND_COLOR_DEC = 51;
	final int BACKGROUND_IMAGE_DEC = 52;
	final int BACKGROUND_POSITION_DEC = 53;
	final int BORDER_COLOR_DEC = 54;
	final int BORDER_STYLE_DEC = 55;
	final int BORDER_WIDTH_DEC = 56;
	final int BOX_SIDE_DEC = 57;
	final int CLEAR_DEC = 58;
	final int CLIP_DEC = 59;
	final int VISIBILITY_DEC = 60;
	final int FLOAT_DEC = 61;
	final int MARGIN_DEC = 62;
	final int PADDING_DEC = 63;
	final int LETTER_DEC = 64;
	final int WORD_SPACING_DEC = 65;
	final int LINE_HEIGHT_DEC = 66;
	final int TEXT_ALIGN_DEC = 67;
	final int TEXT_INDENT_DEC = 68;
	final int TEXT_TRANSFORM_DEC = 69;
	final int WHITE_SPACE_DEC = 70;
	final int TEXT_DECOR_DEC = 71;
	final int FONT_FAMILY_DEC = 72;
	final int FONT_SIZE_DEC = 73;
	final int FONT_STYLE_DEC = 74;
	final int FONT_VARIANT_DEC = 75;
	final int FONT_WEIGHT_DEC = 76;
	final int BORDER_COLLAPSE_DEC = 77;
	final int BORDER_SPACING_DEC = 78;
	final int CAPTION_SIDE_DEC = 79;
	final int EMPTY_CELLS_DEC = 80;
	final int TABLE_LAYOUT_DEC = 81;

	
	//Character classes
	final int DIGIT_CLASS = 1;//if next char is a number
	final int LETTER_CLASS = 2;//if next char is a letter
	final int STRING_CLASS = 3;//if next char is a quotation mark (start of a string)
	final int CLASS_SELECTOR_CLASS = 4;//if next char is a '.' operator for selectors
	final int HASHMARK_CLASS = 5;//if next char is a '#' operator for selectors
	final int COLON_CLASS = 6;//if next char is a colon (for either valid selector states or declarations)
	final int SPECIAL_OP_CLASS = 7;//if next char is a special operator for selectors and attributes
	final int UNKNOWN = 99;//if next char requires a lookup to decipher meaning (special single character case)

	/**
	 * The Constructor of the Lexical Analyzer:
	 * Takes in a file and attempts to develop a buffered reader for the file object. Initializes
	 * the position and lexeme file as well.
	 * 
	 * @param _testFile : the file to be processed and tested for a valid Java For Loop
	 */
	public Lex(File _testFile){
		lexeme = new char[100];
		pos = 0;
		line_pos = 0;
		line_num = 1;

		testFile = _testFile;
		try {
			reader = new FileReader(testFile);
			grabber = new BufferedReader(reader);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The method responsible for grabbing the nextChar for the lexical analyzer.
	 * This method changes the value of nextChar and changes the class to be
	 * respective of its new character.
	 * 
	 */
	public void getChar(){
		try {
			if((c = grabber.read())!=-1){
				if(Character.isAlphabetic((nextChar = (char)c)) || nextChar == '-' || nextChar == '_'){
					charClass = LETTER_CLASS;
				}else if(nextChar == '.'){
					charClass = CLASS_SELECTOR_CLASS;
				}else if(nextChar == '#'){
					charClass = HASHMARK_CLASS;
				}else if(Character.isDigit(nextChar)){
					charClass = DIGIT_CLASS;
				}else if(nextChar == ':'){
					charClass = COLON_CLASS;
				}
				else if(nextChar == '=' || nextChar == '~' || nextChar == '|' ||
						nextChar == '|' || nextChar == '^' || nextChar == '$' ||
						nextChar == '*' || nextChar == '>' || nextChar == '+'){
					charClass = SPECIAL_OP_CLASS;
				}else if(nextChar == '"'){
					charClass = STRING_CLASS;
				}else if(nextChar == '\n'){
					line_num++;
					line_pos = -1;
				}else{
					charClass = UNKNOWN;
				}
			}else{
				charClass = -1;
				nextChar = '\0';
			}
			line_pos++;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The main method of the lexical analyzer that returns the nextToken for the
	 * lexeme that was analyzed from the file. It process the file to the next
	 * nonblank character and then processes the lexeme based of of the characters
	 * respective charClass.
	 * 
	 * @return the token number of the lexeme
	 */
	public int lex(){
		lexeme = new char[100];//The lexeme that will be processed
		pos = 0;//The position inside the lexeme array. Array index essentially
		getNonBlank();//calls nextChar until the nextChar is not a /t /n <space> etc
		switch(charClass){
		case CLASS_SELECTOR_CLASS:
			addChar();
			getChar();
			while(charClass == LETTER_CLASS || charClass == DIGIT_CLASS){
				addChar();
				getChar();
			}
			if(pos == 1){
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Class selector is empty at line : "+line_num+" col : "+line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				Syntax_Driver.errno++;
				nextToken = -1;
				break;
			}
			nextToken = CLASS_S;
			break;
		case HASHMARK_CLASS:
			boolean valid_hex = false;
			addChar();
			getChar();
			while(charClass == LETTER_CLASS || charClass == DIGIT_CLASS){
				addChar();
				getChar();
			}
			if(Syntax_Driver.selector_type){
				if(pos == 1){
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Error in CSS Document:: Id selector is empty at line :"+line_num+" col :"+line_pos);
					System.out.println("------------------------------------------------------------------------------------------------------");
					Syntax_Driver.errno++;
					nextToken = -1;
					break;
				}
				nextToken = ID_S;
				break;
			}else if(Syntax_Driver.color_value){
				if((pos == 4 || pos == 7) && checkHexColor()){
					nextToken = HEX_COLOR;
				}else{
					nextToken = -1;
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Error in CSS Document:: Invalid hexadecimal color at line :"+line_num+" col :"+line_pos);
					System.out.println("------------------------------------------------------------------------------------------------------");
					Syntax_Driver.errno++;
				}
				break;
			}
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Invalid '#' character at line :"+line_num+" col :"+line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			Syntax_Driver.errno++;
			nextToken = -1;
			break;
		case LETTER_CLASS:
			addChar();
			getChar();
			if(Syntax_Driver.color_value || Syntax_Driver.url_tag || Syntax_Driver.clip){
				while(charClass == LETTER_CLASS){
					addChar();
					getChar();
					if(nextChar == '('){
						addChar();
						getChar();
						break;
					}
				}
			}else if(Syntax_Driver.url){
				while(nextChar != ')'){
					addChar();
					getChar();
				}
			}else{
				while(charClass == LETTER_CLASS || charClass == DIGIT_CLASS || nextChar == '-'  || nextChar == '_'){
						addChar();
						getChar();
					}
			}
			if(Syntax_Driver.selector_type){
				nextToken = ELEMENT_S;
			}else if(!checkKeyWord())
				nextToken = STRING_UNBOUND;
			break;
		case DIGIT_CLASS:
			addChar();
			getChar();
			while(charClass == DIGIT_CLASS){
				addChar();
				getChar();
			}
			if(Syntax_Driver.dim_value){
				if(nextChar == '%'){
					addChar();
					getChar();
					nextToken = DIM_VALUE;
					break;
				}else if(nextChar == 'c'){
					addChar();
					getChar();
					if(nextChar == 'm'){
						addChar();
						getChar();
						nextToken = DIM_VALUE;
						break;
					}
				}else if(nextChar == 'p'){
					addChar();
					getChar();
					if(nextChar == 'x' || nextChar == 't'){
						addChar();
						getChar();
						nextToken = DIM_VALUE;
						break;
					}
				}
			}
			if(Syntax_Driver.color_value){
				String num = new String(lexeme);
				num = num.substring(0, pos);
				int i = Integer.parseInt(num);
				if(i > 255 || i < 0){
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Warning in CSS Document:: RGB value is out of range: Ensure that any value is between 0 and 255 at line :"+line_num+" col :"+(line_pos+1));
					System.out.println("------------------------------------------------------------------------------------------------------");
				}
			}
			if(Syntax_Driver.font_weight){
				String num = new String(lexeme);
				num = num.substring(0, pos);
				int i = Integer.parseInt(num);
				if(i != 100 && i != 200 && i != 300 && i!= 400 && i != 500 && i != 600 && i != 700 && i != 800 && i != 900){
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Waring in CSS Document:: Font weight needs to be an even hundred value less than or equal to 900 at line :"+line_num+" col :"+(line_pos+1));
					System.out.println("------------------------------------------------------------------------------------------------------");
				}
			}
			nextToken = NUMBER;
			break;
		case COLON_CLASS:
			addChar();
			if(Syntax_Driver.valid_type){
				getChar();
				while(nextChar == ':' || charClass == LETTER_CLASS || nextChar == '-'){
					addChar();
					getChar();
				}
				if(checkKeyWord()){
					nextToken = VALID_STATE;
				}else{
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Error in CSS Document:: Found a colon that should have lead to a valid state at line: "+line_num+" col: "+(line_pos+1)
							+ "\nEnsure any state is spelled correctly or that the selector list is properly closed.");
					System.out.println("------------------------------------------------------------------------------------------------------");
					nextToken = -1;
					Syntax_Driver.errno++;
				}
			}else{
				nextToken = COLON;
				getChar();
			}
			break;
		case SPECIAL_OP_CLASS:
			if(nextChar == '='){
				addChar();
				nextToken = ATTRIB_OP;
			}else if(nextChar == '~'){
				addChar();
				getChar();
				if(nextChar == '='){
					addChar();
					nextToken = ATTRIB_OP;
				}else{
					nextToken = SELECTOR_OP;
					break;
				}
			}else if(nextChar == '>' || nextChar == '+'){
				addChar();
				getChar();
				nextToken = SELECTOR_OP;
			}else{
				addChar();
				getChar();
				if(nextChar == '='){
					addChar();
					nextToken = ATTRIB_OP;
				}else{
					nextToken = -1;
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Error in CSS Document:: Unexpected lexeme: Check for mistypes and other related errors at line :"+line_num+" col :"+(line_pos+1));
					System.out.println("------------------------------------------------------------------------------------------------------");
					Syntax_Driver.errno++;
				}
			}
			getChar();
			
			break;
		case STRING_CLASS:
			addChar();
			getChar();
			int quote_num = line_num;
			int quote_pos = line_pos+1;
			while(nextChar != '"'){
				addChar();
				getChar();
				if(charClass == -1){
					nextToken = -1;
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Error in CSS Document:: Expected right quotation mark after the left quoation mark at line :"+quote_num+" col :"+quote_pos);
					System.out.println("------------------------------------------------------------------------------------------------------");
					Syntax_Driver.errno++;
					break;
				}
			}
			addChar();
			getChar();
			nextToken = STRING;
			break;
		case UNKNOWN://Looks up special single character special characters
			lookUp(nextChar);
			getChar();
			break;
		case -1:
			nextToken = -1;
			break;
		}

		if(nextToken != -1 && Syntax_Driver.traversal_data == 1)
			System.out.println("Next token:"+nextToken+", Next lexeme is "+report);
		return nextToken;
	}
	
	/**
	 * Runs used part of the lexeme against a string literal representing the
	 * keyword word. If there is a match, nextToken is set accordingly and
	 * the method returns true. Otherwise no effect on nextToken and the
	 * method returns false
	 * 
	 * @return true or false
	 */
	private boolean checkKeyWord() {
		String test_string = new String(lexeme);
		test_string = test_string.toLowerCase();
		
		if(Syntax_Driver.valid_type){
			if(test_string.contains(":active") && pos == 7)
				return true;
			else if(test_string.contains("::after") && pos == 7)
				return true;
			else if(test_string.contains("::before") && pos == 8)
				return true;
			else if(test_string.contains(":checked") && pos == 8)
				return true;
			else if(test_string.contains(":disabled") && pos == 9)
				return true;
			else if(test_string.contains(":empty") && pos == 6)
				return true;
			else if(test_string.contains(":enabled") && pos == 8)
				return true;
			else if(test_string.contains(":first-child") && pos == 12)
				return true;
			else if(test_string.contains("::first-letter") && pos == 14)
				return true;
			else if(test_string.contains(":first-of-type") && pos == 14)
				return true;
			else if(test_string.contains(":focus") && pos == 6)
				return true;
			else if(test_string.contains(":hover") && pos == 6)
				return true;
			else if(test_string.contains(":in-range") && pos == 9)
				return true;
			else if(test_string.contains(":invalid") && pos == 8)
				return true;
			else if(test_string.contains(":last-child") && pos == 11)
				return true;
			else if(test_string.contains(":last-of-type") && pos == 13)
				return true;
			else if(test_string.contains(":link") && pos == 5)
				return true;
			else if(test_string.contains("::first-line") && pos == 12)
				return true;
			else if(test_string.contains(":only-of-type") && pos == 13)
				return true;
			else if(test_string.contains(":only-child") && pos == 11)
				return true;
			else if(test_string.contains(":optional") && pos == 9)
				return true;
			else if(test_string.contains(":out-of-range") && pos == 13)
				return true;
			else if(test_string.contains(":read-only") && pos == 10)
				return true;
			else if(test_string.contains(":read-write") && pos == 11)
				return true;
			else if(test_string.contains(":required") && pos == 9)
				return true;
			else if(test_string.contains("::selection") && pos == 11)
				return true;
			else if(test_string.contains(":target") && pos == 7)
				return true;
			else if(test_string.contains(":valid") && pos == 6)
				return true;
			else if(test_string.contains(":visited") && pos == 8)
				return true;
		}
		
		

		if(Syntax_Driver.color_value){
			if(test_string.contains("black") && pos == 5){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("blue") && pos == 4){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("brown") && pos == 5){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("cyan") && pos == 4){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("grey") && pos == 4){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("gray") && pos == 4){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("green") && pos == 5){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("pink") && pos == 4){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("purple") && pos == 6){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("red") && pos == 3){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("white") && pos == 5){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("yellow") && pos == 6){
				nextToken = COLOR_NAME;
				return true;
			}else if(test_string.contains("rgb(") && pos == 4){
				nextToken = RGB_COLOR;
				return true;
			}
		}
		if(Syntax_Driver.background_position){	
			if(old_value.equals(test_string)){
				nextToken = -1;
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: You've seem to have repeated a value for background-position at line :"+line_num+" col :"+line_pos
						+"\nEnsure that you did not repeat any one of the valid values for backround-position");
				System.out.println("------------------------------------------------------------------------------------------------------");
				Syntax_Driver.errno++;
				return false;
			}else{
				old_value = test_string;
			}
			
			if(test_string.contains("left") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("right") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("top") && pos == 3){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("bottom") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("center") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		if(Syntax_Driver.border_style){
			if(test_string.contains("none") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true; 
			}else if(test_string.contains("hidden") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("dotted") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("dashed") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("solid") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("double") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("groove") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("ridge") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("inset") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("outset") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.border_width){
			if(test_string.contains("medium") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("thin") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("thick") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.box_side){
			if(test_string.contains("auto") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.clear){
			if(test_string.contains("none") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("left") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("right") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("both") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.visibility){
			if(test_string.contains("visible") && pos == 7){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("hidden") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("collapse") && pos == 8){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.clip){
			if(test_string.contains("rect(") && pos == 5){
				nextToken = RECT;
				return true;
			}else if(test_string.contains("auto") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.float_dec){
			if(test_string.contains("none") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("left") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("right") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.margpad){
			if(test_string.contains("auto") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.spacing || Syntax_Driver.line_height){
			if(test_string.contains("normal") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.text_align){
			if(test_string.contains("left") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("right") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("center") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("justify") && pos == 7){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.text_transform){
			if(test_string.contains("none") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("capitalize") && pos == 10){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("uppercase") && pos == 9){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("lowercase") && pos == 9){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.white_space){
			if(test_string.contains("normal") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("nowrap") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("pre") && pos == 3){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("pre-line") && pos == 8){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("pre-wrap") && pos == 8){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.text_decor){
			if(test_string.contains("none") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("underline") && pos == 9){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("overline") && pos == 8){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("line-through") && pos == 12){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.font_size){
			if(test_string.contains("medium") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("xx-small") && pos == 8){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("x-small") && pos == 7){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("small") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("large") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("x-large") && pos == 7){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("xx-large") && pos == 8){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("larger") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("smaller") && pos == 7){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.font_style){
			if(test_string.contains("normal") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("italic") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("oblique") && pos == 7){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.font_variant){
			if(test_string.contains("normal") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("small-caps") && pos == 10){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.font_weight){
			if(test_string.contains("normal") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("bold") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("bolder") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("lighter") && pos == 7){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.border_collapse){
			if(test_string.contains("separate") && pos == 8){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("collapse") && pos == 8){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.caption_side){
			if(test_string.contains("top") && pos == 3){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("bottom") && pos == 6){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.empty_cells){
			if(test_string.contains("show") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("hide") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(Syntax_Driver.table_layout){
			if(test_string.contains("auto") && pos == 4){
				nextToken = VALID_KEYWORD;
				return true;
			}else if(test_string.contains("fixed") && pos == 5){
				nextToken = VALID_KEYWORD;
				return true;
			}
		}
		
		if(test_string.contains("url(") && pos == 4){
			nextToken = URL;
			return true;
		}
	
		
		if(Syntax_Driver.valid_dec){
			if(test_string.contains("color") && pos == 5){
				nextToken = COLOR_DEC;
				return true;
			}else if(test_string.contains("background-color") && pos == 16){
				nextToken = BACKGROUND_COLOR_DEC;
				return true;
			}else if(test_string.contains("background-image") && pos == 16){
				nextToken = BACKGROUND_IMAGE_DEC;
				return true;
			}else if(test_string.contains("background-position") && pos == 19){
				nextToken = BACKGROUND_POSITION_DEC;
				return true;
			}else if(test_string.contains("border-bottom-color") && pos == 19){
				nextToken = BORDER_COLOR_DEC;
				return true;
			}else if(test_string.contains("border-top-color") && pos == 16){
				nextToken = BORDER_COLOR_DEC;
				return true;
			}else if(test_string.contains("border-left-color") && pos == 17){
				nextToken = BORDER_COLOR_DEC;
				return true;
			}else if(test_string.contains("border-right-color") && pos == 18){
				nextToken = BORDER_COLOR_DEC;
				return true;
			}else if(test_string.contains("border-color") && pos == 12){
				nextToken = BORDER_COLOR_DEC;
				return true;
			}else if(test_string.contains("border-bottom-style") && pos == 19){
				nextToken = BORDER_STYLE_DEC;
				return true;
			}else if(test_string.contains("border-top-style") && pos == 16){
				nextToken = BORDER_STYLE_DEC;
				return true;
			}else if(test_string.contains("border-left-style") && pos == 17){
				nextToken = BORDER_STYLE_DEC;
				return true;
			}else if(test_string.contains("border-right-style") && pos == 18){
				nextToken = BORDER_STYLE_DEC;
				return true;
			}else if(test_string.contains("border-style") && pos == 12){
				nextToken = BORDER_STYLE_DEC;
				return true;
			}else if(test_string.contains("border-bottom-width") && pos == 19){
				nextToken = BORDER_WIDTH_DEC;
				return true;
			}else if(test_string.contains("border-top-width") && pos == 16){
				nextToken = BORDER_WIDTH_DEC;
				return true;
			}else if(test_string.contains("border-left-width") && pos == 17){
				nextToken = BORDER_WIDTH_DEC;
				return true;
			}else if(test_string.contains("border-right-width") && pos == 18){
				nextToken = BORDER_WIDTH_DEC;
				return true;
			}else if(test_string.contains("border-width") && pos == 12){
				nextToken = BORDER_WIDTH_DEC;
				return true;
			}else if(test_string.contains("bottom") && pos == 6){
				nextToken = BOX_SIDE_DEC;
				return true;
			}else if(test_string.contains("top") && pos == 3){
				nextToken = BOX_SIDE_DEC;
				return true;
			}else if(test_string.contains("left") && pos == 4){
				nextToken = BOX_SIDE_DEC;
				return true;
			}else if(test_string.contains("right") && pos == 5){
				nextToken = BOX_SIDE_DEC;
				return true;
			}else if(test_string.contains("height") && pos == 6){
				nextToken = BOX_SIDE_DEC;
				return true;
			}else if(test_string.contains("clear") && pos == 5){
				nextToken = CLEAR_DEC;
				return true;
			}else if(test_string.contains("clip") && pos == 4){
				nextToken = CLIP_DEC;
				return true;
			}else if(test_string.contains("visibility") && pos == 10){
				nextToken = VISIBILITY_DEC;
				return true;
			}else if(test_string.contains("float") && pos == 5){
				nextToken = FLOAT_DEC;
				return true;
			}else if(test_string.contains("margin-bottom") && pos == 13){
				nextToken = MARGIN_DEC;
				return true;
			}else if(test_string.contains("margin-left") && pos == 11){
				nextToken = MARGIN_DEC;
				return true;
			}else if(test_string.contains("margin-right") && pos == 12){
				nextToken = MARGIN_DEC;
				return true;
			}else if(test_string.contains("margin-top") && pos == 10){
				nextToken = MARGIN_DEC;
				return true;
			}else if(test_string.contains("padding-bottom") && pos == 14){
				nextToken = MARGIN_DEC;
				return true;
			}else if(test_string.contains("padding-left") && pos == 12){
				nextToken = MARGIN_DEC;
				return true;
			}else if(test_string.contains("padding-right") && pos == 13){
				nextToken = MARGIN_DEC;
				return true;
			}else if(test_string.contains("padding-top") && pos == 11){
				nextToken = PADDING_DEC;
				return true;
			}else if(test_string.contains("letter-spacing") && pos == 14){
				nextToken = LETTER_DEC;
				return true;
			}else if(test_string.contains("word-spacing") && pos == 12){
				nextToken = WORD_SPACING_DEC;
				return true;
			}else if(test_string.contains("line-height") && pos == 11){
				nextToken = LINE_HEIGHT_DEC;
				return true;
			}else if(test_string.contains("text-align") && pos == 10){
				nextToken = TEXT_ALIGN_DEC;
				return true;
			}else if(test_string.contains("text-indent") && pos == 11){
				nextToken = TEXT_INDENT_DEC;
				return true;
			}else if(test_string.contains("text-transform") && pos == 14){
				nextToken = TEXT_TRANSFORM_DEC;
				return true;
			}else if(test_string.contains("white-space") && pos == 11){
				nextToken = WHITE_SPACE_DEC;
				return true;
			}else if(test_string.contains("text-decoration") && pos == 15){
				nextToken = TEXT_DECOR_DEC;
				return true;
			}else if(test_string.contains("font-family") && pos == 11){
				nextToken = FONT_FAMILY_DEC;
				return true;
			}else if(test_string.contains("font-size") && pos == 9){
				nextToken = FONT_SIZE_DEC;
				return true;
			}else if(test_string.contains("font-style") && pos == 10){
				nextToken = FONT_STYLE_DEC;
				return true;
			}else if(test_string.contains("font-variant") && pos == 12){
				nextToken = FONT_VARIANT_DEC;
				return true;
			}else if(test_string.contains("font-weight") && pos == 11){
				nextToken = FONT_WEIGHT_DEC;
				return true;
			}else if(test_string.contains("border-collapse") && pos == 15){
				nextToken = BORDER_COLLAPSE_DEC;
				return true;
			}else if(test_string.contains("border-spacing") && pos == 14){
				nextToken = BORDER_SPACING_DEC;
				return true;
			}else if(test_string.contains("caption-side") && pos == 12){
				nextToken = CAPTION_SIDE_DEC;
				return true;
			}else if(test_string.contains("empty-cells") && pos == 11){
				nextToken = EMPTY_CELLS_DEC;
				return true;
			}else if(test_string.contains("table-layout") && pos == 12){
				nextToken = TABLE_LAYOUT_DEC;
				return true;
			}
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a valid declaration that the current lexeme did not match at line :"+line_num+" col :"+line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			Syntax_Driver.errno++;
		}
		
		return false;
	}
	
	/**Skips white-spacing in a file's contents
	 */
	public void getNonBlank(){
		while(Character.isWhitespace(nextChar)){
			getChar();
		}
	}
	
	/**
	 * Grabs a character from the file and assigns it to nextChar, using
	 * a buffered reader "grabber". Errors out if the lexeme is considered
	 * too long for the lexeme array.
	 */
	public void addChar(){
		if(pos<=98){
			lexeme[pos++] = nextChar;
		}else{
			System.out.println("Lexeme is too long to receive");
			nextToken = -1;
		}
		report = new String(lexeme);
	}
	
	/**
	 * Checks a character "c" and checks if it equals any special characters
	 * in the programming language parameters. If it does it changes nextToken
	 * accordingly and ends. Else it will print an error as this method is a
	 * "last stand" that attempts to find a token for a lexeme.
	 * 
	 * @param c the character being inspected
	 */
	public void lookUp(char c){
		switch(c){
		case ':':
			nextToken = COLON;
			addChar();
			break;
		case '[':
			nextToken = LEFT_BRACKET;
			addChar();
			break;
		case ']':
			nextToken = RIGHT_BRACKET;
			addChar();
			break;
		case ';':
			nextToken = SEMICOL;
			addChar();
			break;
		case ',':
			nextToken = COMMA;
			addChar();
			break;
		case ')':
			nextToken = RIGHT_PAREN;
			addChar();
			break;
		case '{':
			nextToken = LEFT_BRACE;
			addChar();
			break;
		case '}':
			nextToken = RIGHT_BRACE;
			addChar();
			break;
		default:
			nextToken = -1;
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Error with character at line :"+line_num+" col :"+line_pos
					+"\nNo valid token for such");
			System.out.println("------------------------------------------------------------------------------------------------------");
			Syntax_Driver.errno++;
			break;
		}
	}
	/**Checks to ensure that a lexeme is a valid hexadecimal color
	 * @return True if the current lexeme is a hexadecimal, false if not
	 */
	private boolean checkHexColor(){
		char[] s = lexeme;
		for(int x = 1; x<pos; x++){
			s[x] = Character.toLowerCase(s[x]);
			if(s[x] != 'a' && s[x] != 'b' && s[x] != 'c' && s[x] != 'd' && s[x] != 'e' && s[x] != 'f' && !Character.isDigit(s[x]))
				return false;
		}
		return true;
	}
}