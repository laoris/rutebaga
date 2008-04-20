package rutebaga.view.game;


import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import rutebaga.controller.command.Command;
import rutebaga.controller.command.list.ElementalList;
import rutebaga.view.drawer.ColorAttribute;
import rutebaga.view.drawer.Drawer;
import rutebaga.view.rwt.*;

public class AvatarCreationScreen extends ViewCompositeComponent {
	
	private static final ColorAttribute background = new ColorAttribute(new Color(255,255,255));

	
	public AvatarCreationScreen(int width, int height, TextFieldListener listener, ElementalList list, Command accept, Command cancel) {
		this.setBounds(new Rectangle(width, height));
		
		TextLabelComponent label = new TextLabelComponent("Name: ");
		label.setLocation(getWidth()/2, getHeight()/3);
		
		this.addChild(label);
		
		TextFieldComponent text = new TextFieldComponent();
		text.addTextFieldListener(listener);
		text.setLocation(getWidth()/2 + 50, getHeight()/3);
		text.setHasFocus(true);
		
		this.addChild(text);
		
		/*
		 
		for(ListElement element : list)
		 	
		 
		 */
		
		ButtonComponent button1 = new ButtonComponent("Create");
		button1.setLocation(getWidth()/2 - 100, getHeight()*2/3);
		button1.setCommand(accept);
		button1.setParent(this);
		
		this.addChild(button1);
		
		ButtonComponent button2 = new ButtonComponent("Cancel");
		button2.setLocation(getWidth()/2 + 100, getHeight()*2/3);
		button2.setCommand(cancel);
		button2.setParent(this);
		
		this.addChild(button2);
	}
	
	public void draw(Drawer draw) {

		draw.setAttribute(background);
		draw.drawRectangle(new Point(0,0), getWidth(), getHeight());
		
		
		super.draw(draw);
	}

}
