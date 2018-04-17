import java.io.*;
import java.util.*;

/**CSS Validator Driver and Syntax Analyzer.
 * Initiates a prompt for whether traversal order should be included and another
 * for taking in the file path of the file that will be validated and checked for
 * proper CSS Syntax (Within the scope of basic CSS level 2 and 1). Upon termination
 * the CSS Validator, it prints a completion message and the number of syntactic
 * errors that were provoked during the operation.
 * 
 * @authors Michael Mueller, Adam Barron, Winston Smith
 *
 */
public class Syntax_Driver{	
	//Creates the lexical analyzer object Lex, designed to grab logical units and break them into respective
	//tokens
	public static Lex lex;
	public static Scanner scan = new Scanner(System.in);

	//Variables that store whether traversal data is printed and
	//an error number that tracks syntactic errors in the document
	static int traversal_data = 0;
	static int errno = 0;
	
	//Variables that track what specific values are being looked for
	//by the Syntax Analyzer. The lexical analyzer is designed to be
	//able to communicate via this variables and derive some form
	//of context on what to look for in the program.
	static public boolean valid_type = false;
	static public boolean valid_dec = false;
	static public boolean color_value = false;
	static public boolean url_tag = false;
	static public boolean url = false;
	static public boolean dim_value = false;
	
	//Similar to above, but  focus on the position in the document
	//rather than the specific values that are being looked for
	static public boolean selector_type = true;
	static public boolean background_position = false;
	static public boolean border_style = false;
	static public boolean border_width = false;
	static public boolean box_side = false;
	static public boolean clear = false;
	static public boolean clip = false;
	static public boolean visibility = false;
	static public boolean float_dec = false;
	static public boolean margpad = false;
	static public boolean spacing = false;
	static public boolean line_height = false;
	static public boolean text_align = false;
	static public boolean text_transform = false;
	static public boolean white_space = false;
	static public boolean text_decor = false;
	static public boolean font_size = false;
	static public boolean font_style = false;
	static public boolean font_variant = false;
	static public boolean font_weight = false;
	static public boolean border_collapse = false;
	static public boolean caption_side = false;
	static public boolean empty_cells = false;
	static public boolean table_layout = false;
	
	
	/**
	 * Syntax analyzer that acts as a driver for the program. Detects whether the
	 * program structure is correct and prints out steps one-by-one, printing errors
	 * if they occur and in what section they occured.
	 * 
	 * @param args N/A
	 */
	public static void main(String[] args){
		//prompts for whether the user wants traversal data for the CSS document
		System.out.println("Enter 1 for traversal data, 0 for just errors");
		traversal_data = Integer.parseInt(scan.nextLine());
		
		//prompts for the filepath for the validator to check
		System.out.println("Enter a filepath to test syntax for:");
		String testFile = scan.nextLine();
		File test = new File(testFile);
		lex = new Lex(test);

		//primes the lexical analyzer with its first character
		lex.getChar();
		
		//continues to loop through the document analyzer rulesets
		while(lex.nextChar != '\0' && errno == 0){
			selector_type = true;//Notifies that selector_types are being searched for
			lex.lex();
			rule_set();
		}
		
		//Prints that the process is done and how many errors were provoked
		System.out.println("CSS VALIDATION COMPLETE (with only "+errno+" errors"+")");

	}
	
	/**Analyzes rule_sets in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : rule_set -> selector_list '{' declaration_list '}'
	 * 
	 */
	private static void rule_set(){
		if(traversal_data == 1) System.out.println("Entering rule_set");
		//Checks the selector_list validity
		selector_list();
		//Starts of there the braces start in the document for error messages
		int brace_start_num = lex.line_num;
		int brace_start_pos = lex.line_pos;
		//Checks for initial left brace
		if(lex.nextToken == lex.LEFT_BRACE){
			valid_dec = true;//Checks for valid declarations for the declaration_list
			lex.lex();
			declaration_list();
		}else{//Prints an error if a left brace wasn't found
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a left brace to begin declarations at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		//Checks for right brace for the left brace
		if(lex.nextToken == lex.RIGHT_BRACE){
			;
		}else{//Prints an error if the right brace was not found
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a right brace to end declaration list started on line :"+brace_start_num+" col :"+brace_start_pos+
					"\nThis error was detected at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		if(traversal_data == 1) System.out.println("Exiting rule_set");
	}
	/**Analyzes selector_list's in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : selector_list -> selector {',' selector}
	 */
	private static void selector_list(){
		if(traversal_data == 1) System.out.println("Entering selector_list");
		//Checks the validity of any selectors in the list
		selector();
		//If a comma is found after a selector, the selector list analyzes another
		//selector in the list
		while(lex.nextToken == lex.COMMA){
			selector_type = true;
			lex.lex();
			selector();
		}
		if(traversal_data == 1) System.out.println("Exiting selector_list");
	}
	/**Analyzes selector's in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : selector -> selector_type {[selector_op] selector_type}
	 */
	private static void selector(){
		if(traversal_data == 1) System.out.println("Entering selector");
		selector_type = true;//Marks that the analyzer is looking for selector_types
		//Checks the validity of a type of selector
		selector_type();
		//Continuously loops through looking for valid types of of selectors
		while(lex.nextToken == lex.CLASS_S || lex.nextToken == lex.ID_S || lex.nextToken == lex.ELEMENT_S || lex.nextToken == lex.SELECTOR_OP){
			selector_type = true;//Continuosly flags selector_types as something to look for
			if(lex.nextToken == lex.SELECTOR_OP)
				lex.lex();
			selector_type();
		}
		if(traversal_data == 1) System.out.println("Exiting selector");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : selector_type -> class_s | id_s | element_s
	 * 
	 */
	private static void selector_type(){
		if(traversal_data == 1) System.out.println("Entering selector_type");
		//Checks to see if the next lexeme was a class or id selector
		//If so, prime the next lexeme and exit. If it was an element
		//then the validity of th element is checked. Worse case, an
		//error is printed
		if(lex.nextToken == lex.CLASS_S || lex.nextToken == lex.ID_S)
			lex.lex();
		else if(lex.nextToken == lex.ELEMENT_S){
			element_s();
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a valid selector name at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		selector_type = false;//No longer are selectors sought for in the program (context change)
		if(traversal_data == 1) System.out.println("Exiting selector_type");
	}
	/**Analyzes element_s' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : element_s -> unboundstring ( state | '[' attribute ']' )
	 */
	private static void element_s(){
		if(traversal_data == 1) System.out.println("Entering element_s");
		valid_type = true;//Flags that valid_types are sought after in current context
		lex.lex();
		Syntax_Driver.selector_type = false;//Selector types are no longer sought after in current context
		//Checks to see if the element selector id is followed by a valid state or attibute. If it is a
		//valid state, the method ends. If it is a left bracket, the method checks the validity of a attribute.
		//Otherwise an error is thrown
		if(lex.nextToken == lex.VALID_STATE){
			lex.lex();
		}else if(lex.nextToken == lex.LEFT_BRACKET){
			int bracket_start_num = lex.line_num;
			int bracket_start_pos = lex.line_pos;
			attribute();
			if(lex.nextToken != lex.RIGHT_BRACKET){
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected right bracket for left bracket at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
			lex.lex();
		}
		valid_type = false;//Flags that valid_types are no longer sought for
		if(traversal_data == 1) System.out.println("Exiting element_s");
	}
	/**Analyzes attributes' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : attribute -> unboundstring [attribute_operator (string | unboundstring)]
	 */
	private static void attribute(){
		if(traversal_data == 1) System.out.println("Entering attribute");
		lex.lex();
		//Checks to see if there is an unbound string for the start. If none, an error is thrown
		if(lex.nextToken != lex.STRING_UNBOUND){
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a valid attribute name at line :"+lex.line_num+" col :"+lex.line_pos
					+"\n Ensure any misspellings and typos for the attribute are corrected.");
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}else{
			lex.lex();
			//Checks if there is an attribute operator, if not the method ends
			if(lex.nextToken == lex.ATTRIB_OP){
				lex.lex();
				//if there was an attribute operator, another string or unbound string is expected
				//if there was no string or unbound string, an error is thrown
				if(lex.nextToken != lex.STRING_UNBOUND && lex.nextToken != lex.STRING){
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Error in CSS Document:: Expected a valid value for the attribute statement at line :"+lex.line_num+"col :"+lex.line_pos+
							"\nThis value can be a bound or unbound string.");
					System.out.println("------------------------------------------------------------------------------------------------------");
					errno++;
				}else{
					lex.lex();
				}
			}
		}
		if(traversal_data == 1) System.out.println("Exiting attribute");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : declaration_list -> declaration {';'<declaration_list>}
	 * (very generic)
	 */
	private static void declaration_list(){
		//Checks if there was any valid declaration. If there was,
		//check its validity. Otherwise, leave the method.
		if(traversal_data == 1) System.out.println("Entering declaration_list");
		valid_dec = false;
		if(lex.nextToken == lex.COLOR_DEC){
			color_dec();
		}else if(lex.nextToken == lex.BACKGROUND_COLOR_DEC){
			background_color_dec();
		}else if(lex.nextToken == lex.BACKGROUND_IMAGE_DEC){
			background_image();
		}else if(lex.nextToken == lex.BACKGROUND_POSITION_DEC){
			background_position();
		}else if(lex.nextToken == lex.BORDER_COLOR_DEC){
			border_color();
		}else if(lex.nextToken == lex.BORDER_STYLE_DEC){
			border_style();
		}else if(lex.nextToken == lex.BORDER_WIDTH_DEC){
			border_width();
		}else if(lex.nextToken == lex.BOX_SIDE_DEC){
			box_side_dec();
		}else if(lex.nextToken == lex.CLEAR_DEC){
			clear_dec();
		}else if(lex.nextToken == lex.CLIP_DEC){
			clip_dec();
		}else if(lex.nextToken == lex.VISIBILITY_DEC){
			visibility_dec();
		}else if(lex.nextToken == lex.FLOAT_DEC){
			float_dec();
		}else if(lex.nextToken == lex.MARGIN_DEC || lex.nextToken == lex.PADDING_DEC){
			margpad_dec();
		}else if(lex.nextToken == lex.LETTER_DEC || lex.nextToken == lex.WORD_SPACING_DEC){
			spacing_dec();
		}else if(lex.nextToken == lex.LINE_HEIGHT_DEC){
			line_height_dec();
		}else if(lex.nextToken == lex.TEXT_ALIGN_DEC){
			text_align_dec();
		}else if(lex.nextToken == lex.TEXT_INDENT_DEC){
			text_indent_dec();
		}else if(lex.nextToken == lex.TEXT_TRANSFORM_DEC){
			text_transform_dec();
		}else if(lex.nextToken == lex.WHITE_SPACE_DEC){
			white_space_dec();
		}else if(lex.nextToken == lex.TEXT_DECOR_DEC){
			text_decor_dec();
		}else if(lex.nextToken == lex.FONT_FAMILY_DEC){
			font_family_dec();
		}else if(lex.nextToken == lex.FONT_SIZE_DEC){
			font_size_dec();
		}else if(lex.nextToken == lex.FONT_STYLE_DEC){
			font_style_dec();
		}else if(lex.nextToken == lex.FONT_VARIANT_DEC){
			font_variant_dec();
		}else if(lex.nextToken == lex.FONT_WEIGHT_DEC){
			font_weight_dec();
		}else if(lex.nextToken == lex.BORDER_COLLAPSE_DEC){
			border_collapse_dec();
		}else if(lex.nextToken == lex.BORDER_SPACING_DEC){
			border_spacing_dec();
		}else if(lex.nextToken == lex.CAPTION_SIDE_DEC){
			caption_side_dec();
		}else if(lex.nextToken == lex.EMPTY_CELLS_DEC){
			empty_cells_dec();
		}else if(lex.nextToken == lex.TABLE_LAYOUT_DEC){
			table_layout_dec();
		}
		//If a semicolon is detected, recursively check for more
		//declarations
		if(lex.nextToken == lex.SEMICOL){
			valid_dec = true;
			lex.lex();
			declaration_list();
		}
		valid_dec = false;//No longer are declarations sought for in this context
		if(traversal_data == 1) System.out.println("Exiting declaration_list");
	}
	/**Analyzes color_decs' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : color_dec -> "color" ':' (rgb_color | hex_color | color_name)
	 */
	private static void color_dec(){
		if(traversal_data == 1) System.out.println("Entering color_dec");
		lex.lex();
		//Checks for the color after the color string, throws error if fails
		if(lex.nextToken == lex.COLON){
			color_value = true;//Flags color values to be found in the lexical analyzer
			lex.lex();
			//Checks for a valid color value, otherwise error is thrown
			if(lex.nextToken == lex.COLOR_NAME){
				lex.lex();
			}else if(lex.nextToken == lex.RGB_COLOR){
				rgb_color();
			}else if(lex.nextToken == lex.HEX_COLOR){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a hexadecimal, rgb, or named value for color at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
			color_value = false;//Unflags color_values
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		if(traversal_data == 1) System.out.println("Exiting color_dec");
	}
	/**Analyzes rgb_colors' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : rgb_color -> "rgb(" number ',' number ',' number ')'
	 */
	private static void rgb_color(){
		if(traversal_data == 1) System.out.println("Entering rgb_color");
		//Marks the beginning of parenthesis for errors
		int paren_start_num = lex.line_num;
		int paren_start_pos = lex.line_pos;
		lex.lex();
		//Seeks out a number, error is thrown otherwise
		if(lex.nextToken == lex.NUMBER){
			lex.lex();
			//Seeks out a comma, error is thrown otherwise
			if(lex.nextToken == lex.COMMA){
				lex.lex();
				//Seeks out a number, error is thrown otherwise
				if(lex.nextToken == lex.NUMBER){
					lex.lex();
					//Seeks out a comma, error is thrown otherwise
					if(lex.nextToken == lex.COMMA){
						lex.lex();
						//Seeks out a number, error is thrown otherwise
						if(lex.nextToken == lex.NUMBER){
							lex.lex();
							//Seeks out a parenthesis, error is thrown otherwise
							if(lex.nextToken == lex.RIGHT_PAREN){
								lex.lex();
							}else{
								System.out.println("------------------------------------------------------------------------------------------------------");
								System.out.println("Error in CSS Document:: Expected a right parentheses to close parantheses at line :"+paren_start_num+" col :"+(paren_start_pos+2)+
										"\nThis error was detected at line :"+lex.line_num+" col :"+lex.line_pos);
								System.out.println("------------------------------------------------------------------------------------------------------");
								errno++;
							}
						}else{
							System.out.println("------------------------------------------------------------------------------------------------------");
							System.out.println("Error in CSS Document:: Expected a number for field 3 of the rgb value at line :"+lex.line_num+" col :"+lex.line_pos);
							System.out.println("------------------------------------------------------------------------------------------------------");
							errno++;
						}
					}else{
						System.out.println("------------------------------------------------------------------------------------------------------");
						System.out.println("Error in CSS Document:: Expected a comma to delimite entry 2 and 3 of the rgb value at line :"+lex.line_num+" col :"+lex.line_pos);
						System.out.println("------------------------------------------------------------------------------------------------------");
						errno++;
					}
				}else{
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Error in CSS Document:: Expected a number for field 3 of the rgb value at line :"+lex.line_num+" col :"+lex.line_pos);
					System.out.println("------------------------------------------------------------------------------------------------------");
					errno++;
				}
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a comma to delimite entry 1 and 2 of the rgb value at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a number for field 3 of the rgb value at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		if(traversal_data == 1) System.out.println("Exiting rgb_color");
	}
	/**Analyzes background_color_decs' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : background_color_dec -> "background-color" ':' (rgb_color | hex_color | color_name)
	 */
	private static void background_color_dec(){
		if(traversal_data == 1) System.out.println("Entering background_color_dec");
		lex.lex();
		//Checks for a colon, error is thrown otherwise
		if(lex.nextToken == lex.COLON){
			color_value = true;//Flags that the context seeks out color values
			lex.lex();
			//Checks for a valid color value. If a rgb_color value is found, checks its validity.
			//If not a valid color value, an error is thrown
			if(lex.nextToken == lex.COLOR_NAME){
				lex.lex();
			}else if(lex.nextToken == lex.RGB_COLOR){
				rgb_color();
			}else if(lex.nextToken == lex.HEX_COLOR){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a hexadecimal, rgb, or named value for background-color at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
			color_value = false;//Unflags color values
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;		
		}
		if(traversal_data == 1) System.out.println("Exiting background_color_dec");
	}
	/**Analyzes background_images' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : background_image -> "background-image" ':' url
	 */
	private static void background_image(){
		if(traversal_data == 1) System.out.println("Entering background_image_dec");
		lex.lex();
		//Seeks out a colon, error is thrown out otherwise
		if(lex.nextToken == lex.COLON){
			url_tag = true;//Flags url_tags to be found in the lexical analyzer
			url = true;//Notifies that the context is within the url method
			lex.lex();
			url_tag = false;//Unflags the url_tag for the context
			//Seeks out the header for the url, otherwise an error is thrown
			if(lex.nextToken == lex.URL){
				lex.charClass = lex.LETTER_CLASS;
				lex.lex();
				url();
				url = false;//Unflags the url context as the method closed
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid url for background-image at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		if(traversal_data == 1) System.out.println("Entering background_image_dec");
	}
	/**Analyzes urls' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : url -> "url(" [(unboundstring | string)] ')' 
	 */
	private static void url(){
		if(traversal_data == 1) System.out.println("Entering url");
		//Marks the beginning of the parenthesis for errors
		int paren_start_num = lex.line_num;
		int paren_start_pos = lex.line_pos;
		//Checks for either a string or unbound string 
		if(lex.nextToken == lex.STRING || lex.nextToken == lex.STRING_UNBOUND){
			lex.lex();
		}
		//Checks to make sure the parenthesis is closed, otherwise error is thrown
		if(lex.nextToken == lex.RIGHT_PAREN){
			lex.lex();
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a right parentheses to close parantheses at line :"+paren_start_num+" col :"+(paren_start_pos+2)+
					"\nThis error was detected at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		if(traversal_data == 1) System.out.println("Exiting url");
	}
	/**Analyzes background_positions' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : background_position -> "background-position" ':' valid_keyword [valid_keyword]
	 */
	private static void background_position(){
		if(traversal_data == 1) System.out.println("Entering background-position");
		lex.lex();
		//Seeks for a colon, otherwise error is thrown
		if(lex.nextToken == lex.COLON){
			background_position = true;//Flags that the declaration context is background_position
			lex.lex();
			//Checks for a valid keyword and then another optional valid keyword, else error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
				if(lex.nextToken == lex.VALID_KEYWORD){
					lex.lex();
				}
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected either one or two valid keywords for background-position at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid keywords are left, right, top, center, bottom");
				System.out.println("------------------------------------------------------------------------------------------------------");
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		background_position = false;//Unflags the context variable
		lex.old_value = "null";//Nullifies the old value in the lexical analyzer so that it is fresh for next declaration
		if(traversal_data == 1) System.out.println("Exiting background-position");
	}
	/**Analyzes border_colors' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : border_color -> "border-color" ':' (rgb_color | color_name | hex_color)
	 */
	private static void border_color(){
		if(traversal_data == 1) System.out.println("Entering border_color");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			color_value = true;//Flags that the context seeks out a color_value
			lex.lex();
			//Checks for a valid color value. If it is an rgb_color, then it checks
			//the validity of it. If it was not a valid color value, an error is
			//thrown
			if(lex.nextToken == lex.COLOR_NAME){
				lex.lex();
			}else if(lex.nextToken == lex.RGB_COLOR){
				rgb_color();
			}else if(lex.nextToken == lex.HEX_COLOR){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a hexadecimal, rgb, or named value for border-color at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
			color_value = false;//UnFlags color_value for the context
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		if(traversal_data == 1) System.out.println("Exiting border_color");
	}
	/**Analyzes border_styles' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : "border-style" ':' valid_keyword
	 */
	private static void border_style(){
		if(traversal_data == 1) System.out.println("Entering border_style");
		lex.lex();
		//Checks for a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			border_style = true;//Flags that the context rests in border-style
			lex.lex();
			//Checks to see if a valid_keyword was found. Otherwise an error
			//is thrown.
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for border-style at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are none, hidden, dotted, dashed, solid, double, groove, ridge, inset, and outset");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		border_style = false;//Unflags the current context
		if(traversal_data == 1) System.out.println("Exiting border_style");
	}
	/**Analyzes border_widths' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : "border-width" ':' (valid_keyword | dim_value)
	 */
	private static void border_width(){
		if(traversal_data == 1) System.out.println("Entering border_width");
		lex.lex();
		//Seeks out a colon
		if(lex.nextToken == lex.COLON){
			border_width = true;//Flags that the context is border_width
			dim_value = true;//Flags that dim_values are being seeked
			lex.lex();
			//Seeks out a valid_keyword or dim_value, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD || lex.nextToken == lex.DIM_VALUE){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword or dim value for border-width at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid keywords are thin, thick, and medium");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		dim_value = false;//Unflags the current context variables
		border_width = false;
		if(traversal_data == 1) System.out.println("Exiting border_width");
	}
	/**Analyzes box_sides' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : box-side ':' (valid_keyword | dim_value)
	 */
	private static void box_side_dec(){
		if(traversal_data == 1) System.out.println("Entering box_side");
		lex.lex();
		//Seeks out a colon, otherwise error is thrown
		if(lex.nextToken == lex.COLON){
			box_side = true;//Flags that the context is box_side
			dim_value = true;//Flags that the context is seeking dim_values
			lex.lex();
			//Seeks out a valid_keyword or dim_value, otherwise error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD || lex.nextToken == lex.DIM_VALUE){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a keyword or dim value for box side at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keyword is auto");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		dim_value = false;//Unflags the context variables
		box_side = false;
		if(traversal_data == 1) System.out.println("Entering box_side");
	}
	/**Analyzes clears' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : "clear" ':' valid_keyword
	 */
	private static void clear_dec(){
		if(traversal_data == 1) System.out.println("Entering clear");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			clear = true;//Flags that the current context variable is clear
			lex.lex();
			//Seeks out a valid_keword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for clear at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are none, left, right, and both");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		clear = false;//Unflags the current context variables
		if(traversal_data == 1) System.out.println("Exiting clear");
	}
	/**Analyzes clips' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : "clip" ':' valid_keyword
	 */
	private static void clip_dec(){
		if(traversal_data == 1) System.out.println("Entering clip");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			clip = true;//Flags that the current context is in clip
			lex.lex();
			//Seeks out a valid_keyword, otherwise checks if it is rectangle, otherwise, error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else if(lex.nextToken == lex.RECT){
				dim_value = true;
				lex.lex();
				rect();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword or rectangle for clip at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keyword is auto");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		clip = false;//Unflags current context variable
		if(traversal_data == 1) System.out.println("Exiting clip");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : rect -> "rect("dim_value ',' dim_value ',' dim_value ',' dim_value ')'
	 */
	private static void rect(){
		if(traversal_data == 1) System.out.println("Entering rect");
		//Marks the beginning of the parenthesis
		int paren_start_num = lex.line_num;
		int paren_start_pos = lex.line_pos;
		//Checks that there is a dim_value, comma, dim_value, comma, dim_value, 
		//comma, dim_value, and a right paren in that order. Otherwise an error
		//is thrown	
		if(lex.nextToken == lex.DIM_VALUE){
			lex.lex();
			if(lex.nextToken == lex.COMMA){
				lex.lex();
				if(lex.nextToken == lex.DIM_VALUE){
					lex.lex();
					if(lex.nextToken == lex.COMMA){
						lex.lex();
						if(lex.nextToken == lex.DIM_VALUE){
							lex.lex();
							if(lex.nextToken == lex.COMMA){
								lex.lex();
								if(lex.nextToken == lex.DIM_VALUE){
									lex.lex();
									if(lex.nextToken == lex.RIGHT_PAREN){
										lex.lex();
									}else{
										System.out.println("------------------------------------------------------------------------------------------------------");
										System.out.println("Error in CSS Document:: Expected a right parentheses to close parantheses at line :"+paren_start_num+" col :"+(paren_start_pos+2)+
												"\nThis error was detected at line :"+lex.line_num+" col :"+lex.line_pos);
										System.out.println("------------------------------------------------------------------------------------------------------");
										errno++;
									}
								}else{
									System.out.println("------------------------------------------------------------------------------------------------------");
									System.out.println("Error in CSS Document:: Expected 4th dimension value for rectangle at line :"+lex.line_num+" col :"+lex.line_pos);
									System.out.println("------------------------------------------------------------------------------------------------------");
									errno++;
								}
							}else{
								System.out.println("------------------------------------------------------------------------------------------------------");
								System.out.println("Error in CSS Document:: Expected comma delimeter for 3rd and 4th entry in rectangle at line :"+lex.line_num+" col :"+lex.line_pos);
								System.out.println("------------------------------------------------------------------------------------------------------");
							}
						}else{
							System.out.println("------------------------------------------------------------------------------------------------------");
							System.out.println("Error in CSS Document:: Expected 3rd dimension value for rectangle at line :"+lex.line_num+" col :"+lex.line_pos);
							System.out.println("------------------------------------------------------------------------------------------------------");
							errno++;
						}
					}else{
						System.out.println("------------------------------------------------------------------------------------------------------");
						System.out.println("Error in CSS Document:: Expected comma delimeter for 2nd and 3rd entry in rectangle at line :"+lex.line_num+" col :"+lex.line_pos);
						System.out.println("------------------------------------------------------------------------------------------------------");
						errno++;
					}
				}else{
					System.out.println("------------------------------------------------------------------------------------------------------");
					System.out.println("Error in CSS Document:: Expected 2nd dimension value for rectangle at line :"+lex.line_num+" col :"+lex.line_pos);
					System.out.println("------------------------------------------------------------------------------------------------------");
					errno++;
				}
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected comma delimeter for 1st and 2nd entry in rectangle at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected 1st dimension value for rectangle at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		if(traversal_data == 1) System.out.println("Exiting rect");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : "visibility" ':' valid_keyword
	 */
	private static void visibility_dec(){
		if(traversal_data == 1) System.out.println("Entering visibility");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			visibility = true;//Flags the context as visibility
			lex.lex();
			//Seeks out a valid_key_word, othwerwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for visibility at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are visible, hidden, and collapse");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		visibility = false;//Unflags the context variable.
		if(traversal_data == 1) System.out.println("Exiting visibility");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : "float" ':' valid_keyword
	 */
	private static void float_dec(){
		if(traversal_data == 1) System.out.println("Entering float");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			float_dec = true;//Flags the context as float
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for float at line :"+lex.line_num+" col :"+lex.line_pos+
						"\n");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		float_dec = false;//Unflags a context variable
		if(traversal_data == 1) System.out.println("Exiting float");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : margpad -> margpad_dec ':' (keyword | dim_value)
	 */
	private static void margpad_dec(){
		if(traversal_data == 1) System.out.println("Entering margin");
		lex.lex();
		//Seeks out a colon, otherwise error is thrown
		if(lex.nextToken == lex.COLON){
			dim_value = true;//Flags that dim_values are being seeked
			margpad = true;//Flags margpad as the current context
			lex.lex();
			//Seeks out a valid_keword or dim_value, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD || lex.nextToken == lex.DIM_VALUE){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword or dim value for declaration at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are auto");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		dim_value = false;//Unflags the current context variables
		margpad = false;
		if(traversal_data == 1) System.out.println("Exiting margin");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : spacing_dec ':' ( valid_keyword | dim_value)
	 */
	private static void spacing_dec(){
		if(traversal_data == 1) System.out.println("Entering Spacing");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			dim_value = true;//Flags that dim_values are being seeked
			spacing = true;//Flags the context as spacing
			lex.lex();
			//Seeks out a valid_keyword or dim_value, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD || lex.nextToken == lex.DIM_VALUE){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword or dim value for spacing at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are normal");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		dim_value = false;//Unflags the context variables
		spacing = false;
		if(traversal_data == 1) System.out.println("Exiting Spacing");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : line_height -> "line-height" ':' (valid_keyword| dim_value | number)
	 */
	private static void line_height_dec(){
		if(traversal_data == 1) System.out.println("Entering line-height");
		lex.lex();
		//Seeks out a colon, otherwise error is thrown
		if(lex.nextToken == lex.COLON){
			dim_value = true;//Flags that the context seeks out a dim_value
			line_height = true;//Flags that the context is line_height
			lex.lex();
			//Seeks out a valid_keyword, dim_value, or number. Otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD || lex.nextToken == lex.DIM_VALUE || lex.nextToken == lex.NUMBER){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword, dim value, or number for spacing at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are normal");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		dim_value = false;//Unflags the context variables
		line_height = false;
		if(traversal_data == 1) System.out.println("Exiting line-height");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : text_align -> "text-align" ':' valid_keyword
	 */
	private static void text_align_dec(){
		if(traversal_data == 1) System.out.println("Entering text-align");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			text_align = true;//Flags the context as text_align
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for text-align at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are left, right, center, and justify");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		text_align = false;//Unflags current context variables
		if(traversal_data == 1) System.out.println("Exiting text-align");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : text_indent_dec -> "text-indent" ':' dim_value
	 */
	private static void text_indent_dec(){
		if(traversal_data == 1) System.out.println("Entering text-indent");
		lex.lex();
		//Seeks out a colon, otherwise throw an error
		if(lex.nextToken == lex.COLON){
			dim_value = true;//Flags that the context seeks a dim_value
			lex.lex();
			//Seeks out a dim_value, otherwise an error is thrown
			if(lex.nextToken == lex.DIM_VALUE){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid dim value for text-indent at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		dim_value = false;//Unflags the context variables
		if(traversal_data == 1) System.out.println("Exiting text-indent");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : text_transform_dec -> "text-transform" ':' valid_keyword
	 */
	private static void text_transform_dec(){
		if(traversal_data == 1) System.out.println("Entering text-transform");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			text_transform = true;//Flags the context as text_transform
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for text-transform at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are none, capitalize, uppercase, and lowercase");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		text_transform = false;//Unflags context variables
		if(traversal_data == 1) System.out.println("Exiting text-transform");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : white_space_dec -> "white-space" ':' valid_keyword
	 */
	private static void white_space_dec(){
		if(traversal_data == 1) System.out.println("Entering white-space");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			white_space = true;//Flags the context as white_space
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for white-space at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are normal, nowrap, pre, and pre-line, pre-wrap");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		white_space = false;//Unflags the current context variable
		if(traversal_data == 1) System.out.println("Exiting white-space");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : text_decor_dec -> "text-decoration" ':' valid_keyword
	 */
	private static void text_decor_dec(){
		if(traversal_data == 1) System.out.println("Entering text-decor");
		lex.lex();
		//seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			text_decor = true;//Flags that the context is text_decor
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for text-decoration at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are none, underline, overline, and line-through");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		text_decor = false;//Unflags current context variable
		if(traversal_data == 1) System.out.println("Exiting text-decor");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : font_family_dec -> "font-family" ':' (string | unbound_string) {, (string | unbound_string)}
	 */
	private static void font_family_dec(){
		if(traversal_data == 1) System.out.println("Entering font-family");
		lex.lex();
		//Seeks a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			lex.lex();
			//Seeks a string or an unbound string, otherwise an error is thrown
			if(lex.nextToken == lex.STRING || lex.nextToken == lex.STRING_UNBOUND){
				lex.lex();
				//Seeks out a comma, if found the list is continued
				while(lex.nextToken == lex.COMMA){
					lex.lex();
					//Seeks out a string or unbound string, otherwise an error is thrown
					if(lex.nextToken == lex.STRING || lex.nextToken == lex.STRING_UNBOUND){
						lex.lex();
					}else{
						System.out.println("------------------------------------------------------------------------------------------------------");
						System.out.println("Error in CSS Document:: Expected another font name after comma at line :"+lex.line_num+" col :"+lex.line_pos);
						System.out.println("------------------------------------------------------------------------------------------------------");
						errno++;
						break;
					}
				}
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid value for declaration at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValues can be unbound or bound strings.");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		if(traversal_data == 1) System.out.println("Exiting font-family");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : font_size_dec -> "font-size" ':' (valid_keyword | dim_value)
	 */
	private static void font_size_dec(){
		if(traversal_data == 1) System.out.println("Entering font-size");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			font_size = true;//Flags the current context is font_size
			dim_value = true;//Flags the dim_value as being seeked
			lex.lex();
			if(lex.nextToken == lex.VALID_KEYWORD || lex.nextToken == lex.DIM_VALUE){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword or dim value for font-size at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid keywords are medium, xx-small, x-small, small, large, x-large, xx-large, larger, and smaller");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		dim_value = false;//Unflags current context variables
		font_size = false;
		if(traversal_data == 1) System.out.println("Exiting font-size");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : font_style_dec -> "font-style" ':' valid_keyword
	 */
	private static void font_style_dec(){
		if(traversal_data == 1) System.out.println("Entering font-style");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			font_style = true;//Flags the current context as font_style
			lex.lex();
			//Seeks out a valid_keyword, othwerwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for font-style at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid keywords are normal, italic, and oblique");
				System.out.println("------------------------------------------------------------------------------------------------------");
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		font_style = false;//Unflags the current context variables
		if(traversal_data == 1) System.out.println("Exiting font-style");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : font_variant_dec -> "font-variant" ':' valid_keyword
	 */
	private static void font_variant_dec(){
		if(traversal_data == 1) System.out.println("Entering font-variant");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			font_variant = true;//Flags the context variable as font_variant
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for font-variant at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid keywords are normal and small-caps");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		font_variant = false;//Unflags the current context variables
		if(traversal_data == 1) System.out.println("Exiting font-variant");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : font_weight_dec -> "font-weight" ':' (valid_keyword | number)
	 */
	private static void font_weight_dec(){
		if(traversal_data == 1) System.out.println("Entering font-weight");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			font_weight = true;//Flags the current context as font_weight
			lex.lex();
			//Seeks out a valid_keyword or a number, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD || lex.nextToken == lex.NUMBER){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword or number for font-weight at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid keywords are normal, bold, bolder, and lighter");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		font_weight = false;//Unflags the context variables
		if(traversal_data == 1) System.out.println("Exiting font-weight");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : 
	 */
	private static void border_collapse_dec(){
		if(traversal_data == 1) System.out.println("Entering border-collapse");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			border_collapse = true;//Flags the current variable as border_collapse
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for border-collapse at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid keywords are separate and collapse");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		border_collapse = false;//Unflags the current context variables
		if(traversal_data == 1) System.out.println("Exiting border-collapse");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : border_spacing_dec -> "border-spacing" ':' dim_values
	 */
	private static void border_spacing_dec(){
		if(traversal_data == 1) System.out.println("Entering border-spacing");
		lex.lex();
		//Seeks a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			dim_value = true;//Flags that the context is seeking a dim_value
			lex.lex();
			//Seeks out a dim_value, otherwise an error is thrown
			if(lex.nextToken == lex.DIM_VALUE){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid dim value for border-spacing at line :"+lex.line_num+" col :"+lex.line_pos);
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		dim_value = false;//Unflags the current context variables
		if(traversal_data == 1) System.out.println("Exiting border-spacing");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : caption_side_dec -> "caption-side-dec" ':' valid_keyword
	 */
	private static void caption_side_dec(){
		if(traversal_data == 1) System.out.println("Entering caption side");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			caption_side = true;//Flags the current context as caption_side
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for caption-side at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid keywords are top and bottom");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		caption_side = false;//Unflags the current context variables
		if(traversal_data == 1) System.out.println("Exiting caption side");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : empty_cells_dec -> "empty-cells" ':' valid_keyword
	 */
	private static void empty_cells_dec(){
		if(traversal_data == 1) System.out.println("Entering empty cells");
		lex.lex();
		//Seeks out a colon, otherwise an error is thrown
		if(lex.nextToken == lex.COLON){
			empty_cells = true;//Flags the current context as empty_cells
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for empty-cells at line :"+lex.line_num+" col :"+lex.line_pos
						+"\nValid Keywords are show and hide");
				System.out.println("------------------------------------------------------------------------------------------------------");
				errno++;
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		empty_cells = false;//Unflags current context variables
		if(traversal_data == 1) System.out.println("Exiting empty cells");
	}
	/**Analyzes selector_types' in the document with the help of the lexical
	 * analyzer that grabs lexemes for it. Prints out formatted error
	 * messages for when errors are provoked in the method.
	 * 
	 * EBNF FORM : table_layout_dec -> "table-layout" ':' valid_keyword
	 */
	private static void table_layout_dec(){
		if(traversal_data == 1) System.out.println("Entering table-layout");
		lex.lex();
		//Seeks out a colon, otherwise throws an error
		if(lex.nextToken == lex.COLON){
			table_layout = true;//Flags the current context as table_layout
			lex.lex();
			//Seeks out a valid_keyword, otherwise an error is thrown
			if(lex.nextToken == lex.VALID_KEYWORD){
				lex.lex();
			}else{
				System.out.println("------------------------------------------------------------------------------------------------------");
				System.out.println("Error in CSS Document:: Expected a valid keyword for table-layout at line :"+lex.line_num+" col :"+lex.line_pos+
						"\nValid keywords are auto and fixed");
				System.out.println("------------------------------------------------------------------------------------------------------");
			}
		}else{
			System.out.println("------------------------------------------------------------------------------------------------------");
			System.out.println("Error in CSS Document:: Expected a colon after declaration at line :"+lex.line_num+" col :"+lex.line_pos);
			System.out.println("------------------------------------------------------------------------------------------------------");
			errno++;
		}
		table_layout = false;//Unflags the current context variable
		if(traversal_data == 1) System.out.println("Exiting table-layout");
	}
}