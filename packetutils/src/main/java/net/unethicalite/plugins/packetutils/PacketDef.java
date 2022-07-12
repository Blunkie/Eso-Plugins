package net.unethicalite.plugins.packetutils;

import java.util.LinkedHashMap;

public class PacketDef
{
	final String name;
	final LinkedHashMap<String,String> fields;
	final PacketType type;

	public static final PacketDef OPOBJ1;
	public static final PacketDef IF_BUTTON9;
	public static final PacketDef IF_BUTTON8;
	public static final PacketDef OPOBJ5;
	public static final PacketDef IF_BUTTON5;
	public static final PacketDef OPOBJ4;
	public static final PacketDef IF_BUTTON4;
	public static final PacketDef OPOBJ3;
	public static final PacketDef IF_BUTTON7;
	public static final PacketDef OPOBJ2;
	public static final PacketDef IF_BUTTON6;
	public static final PacketDef OPLOCT;
	public static final PacketDef OPNPCT;
	public static final PacketDef OPPLAYERT;
	public static final PacketDef OPOBJT;
	public static final PacketDef IF_BUTTONT;
	public static final PacketDef OPNPC2;
	public static final PacketDef OPPLAYER6;
	public static final PacketDef OPNPC3;
	public static final PacketDef OPPLAYER7;
	public static final PacketDef OPLOC2;
	public static final PacketDef OPPLAYER8;
	public static final PacketDef OPLOC1;
	public static final PacketDef OPNPC1;
	public static final PacketDef OPLOC4;
	public static final PacketDef OPPLAYER2;
	public static final PacketDef OPLOC3;
	public static final PacketDef OPPLAYER3;
	public static final PacketDef OPNPC4;
	public static final PacketDef OPPLAYER4;
	public static final PacketDef OPNPC5;
	public static final PacketDef OPPLAYER5;
	public static final PacketDef OPLOC5;
	public static final PacketDef OPPLAYER1;
	public static final PacketDef MOVE_GAMECLICK;
	public static final PacketDef IF_BUTTON1;
	public static final PacketDef IF_BUTTON3;
	public static final PacketDef IF_BUTTON2;
	public static final PacketDef EVENT_MOUSE_CLICK;
	public static final PacketDef IF_BUTTON10;
	public static final PacketDef RESUME_PAUSEBUTTON;

	PacketDef(String var1, LinkedHashMap fields, PacketType type) {
		this.name = var1;
		this.fields = fields;
		this.type = type;
	}
	static
	{
		RESUME_PAUSEBUTTON = new PacketDef(ObfuscatedNames.RESUME_PAUSEBUTTON, new LinkedHashMap<String,String>(){{
			put("childId", "dd");
			put("widgetId", "do");
		}}, PacketType.RESUME_PAUSEBUTTON);
		OPOBJ1 = new PacketDef(ObfuscatedNames.OPOBJ1, new LinkedHashMap<String,String>(){
			{
				put("worldPointX","dr");
				put("groundItemId","dz");
				put("worldPointY","au");
				put("ctrlDown","aw");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON9 = new PacketDef(ObfuscatedNames.IF_BUTTON9, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		IF_BUTTON8 = new PacketDef(ObfuscatedNames.IF_BUTTON8, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		OPOBJ5 = new PacketDef(ObfuscatedNames.OPOBJ5, new LinkedHashMap<String,String>(){
			{
				put("worldPointX","dz");
				put("ctrlDown","cb");
				put("groundItemId","dr");
				put("worldPointY","au");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON5 = new PacketDef(ObfuscatedNames.IF_BUTTON5, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		OPOBJ4 = new PacketDef(ObfuscatedNames.OPOBJ4, new LinkedHashMap<String,String>(){
			{
				put("worldPointX","dz");
				put("groundItemId","dr");
				put("ctrlDown","ck");
				put("worldPointY","au");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON4 = new PacketDef(ObfuscatedNames.IF_BUTTON4, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		OPOBJ3 = new PacketDef(ObfuscatedNames.OPOBJ3, new LinkedHashMap<String,String>(){
			{
				put("worldPointY","au");
				put("ctrlDown","cb");
				put("groundItemId","dz");
				put("worldPointX","dd");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON7 = new PacketDef(ObfuscatedNames.IF_BUTTON7, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		OPOBJ2 = new PacketDef(ObfuscatedNames.OPOBJ2, new LinkedHashMap<String,String>(){
			{
				put("worldPointY","dr");
				put("groundItemId","dr");
				put("ctrlDown","ca");
				put("worldPointX","dr");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON6 = new PacketDef(ObfuscatedNames.IF_BUTTON6, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		OPLOCT = new PacketDef(ObfuscatedNames.OPLOCT, new LinkedHashMap<String,String>(){
			{
				put("worldPointY","dd");
				put("objectId","au");
				put("worldPointX","dr");
				put("sourceSlot","dd");
				put("ctrlDown","ck");
				put("sourceWidgetId","dq");
				put("sourceItemId","dz");
			}
		}, PacketType.OPLOCT);
		OPNPCT = new PacketDef(ObfuscatedNames.OPNPCT, new LinkedHashMap<String,String>(){
			{
				put("sourceWidgetId","dn");
				put("sourceSlot","au");
				put("sourceItemId","au");
				put("npcIndex","dr");
				put("ctrlDown","aw");
			}
		}, PacketType.OPNPCT);
		OPPLAYERT = new PacketDef(ObfuscatedNames.OPPLAYERT, new LinkedHashMap<String,String>(){
			{
				put("sourceSlot","au");
				put("playerIndex","dr");
				put("ctrlDown","ck");
				put("sourceWidgetId","as");
				put("sourceItemId","dd");
			}
		}, PacketType.OPPLAYERT);
		OPOBJT = new PacketDef(ObfuscatedNames.OPOBJT, new LinkedHashMap<String,String>(){
			{
				put("groundItemId","dd");
				put("ctrlDown","aw");
				put("worldPointX","dz");
				put("sourceSlot","dr");
				put("sourceItemId","dd");
				put("worldPointY","dz");
				put("sourceWidgetId","dn");
			}
		}, PacketType.OPOBJT);
		IF_BUTTONT = new PacketDef(ObfuscatedNames.IF_BUTTONT, new LinkedHashMap<String,String>(){
			{
				put("sourceItemId","dr");
				put("sourceSlot","dz");
				put("sourceWidgetId","dn");
				put("itemId","dr");
				put("destinationSlot","au");
				put("widgetId","do");
			}
		}, PacketType.IF_BUTTONT);
		OPNPC2 = new PacketDef(ObfuscatedNames.OPNPC2, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","ck");
				put("npcIndex","dd");
			}
		}, PacketType.OPNPC);
		OPPLAYER6 = new PacketDef(ObfuscatedNames.OPPLAYER6, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","dd");
				put("ctrlDown","ca");
			}
		}, PacketType.OPPLAYER);
		OPNPC3 = new PacketDef(ObfuscatedNames.OPNPC3, new LinkedHashMap<String,String>(){
			{
				put("npcIndex","dz");
				put("ctrlDown","ca");
			}
		}, PacketType.OPNPC);
		OPPLAYER7 = new PacketDef(ObfuscatedNames.OPPLAYER7, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","au");
				put("ctrlDown","aw");
			}
		}, PacketType.OPPLAYER);
		OPLOC2 = new PacketDef(ObfuscatedNames.OPLOC2, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","ca");
				put("worldPointY","au");
				put("worldPointX","au");
				put("objectId","dr");
			}
		}, PacketType.OPLOC);
		OPPLAYER8 = new PacketDef(ObfuscatedNames.OPPLAYER8, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","ck");
				put("playerIndex","au");
			}
		}, PacketType.OPPLAYER);
		OPLOC1 = new PacketDef(ObfuscatedNames.OPLOC1, new LinkedHashMap<String,String>(){
			{
				put("worldPointY","dr");
				put("worldPointX","dr");
				put("ctrlDown","ck");
				put("objectId","dd");
			}
		}, PacketType.OPLOC);
		OPNPC1 = new PacketDef(ObfuscatedNames.OPNPC1, new LinkedHashMap<String,String>(){
			{
				put("npcIndex","dz");
				put("ctrlDown","cb");
			}
		}, PacketType.OPNPC);
		OPLOC4 = new PacketDef(ObfuscatedNames.OPLOC4, new LinkedHashMap<String,String>(){
			{
				put("worldPointY","dr");
				put("worldPointX","au");
				put("ctrlDown","ca");
				put("objectId","dz");
			}
		}, PacketType.OPLOC);
		OPPLAYER2 = new PacketDef(ObfuscatedNames.OPPLAYER2, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","dz");
				put("ctrlDown","ca");
			}
		}, PacketType.OPPLAYER);
		OPLOC3 = new PacketDef(ObfuscatedNames.OPLOC3, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","ck");
				put("worldPointX","dz");
				put("worldPointY","dz");
				put("objectId","au");
			}
		}, PacketType.OPLOC);
		OPPLAYER3 = new PacketDef(ObfuscatedNames.OPPLAYER3, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","dr");
				put("ctrlDown","ck");
			}
		}, PacketType.OPPLAYER);
		OPNPC4 = new PacketDef(ObfuscatedNames.OPNPC4, new LinkedHashMap<String,String>(){
			{
				put("npcIndex","au");
				put("ctrlDown","cb");
			}
		}, PacketType.OPNPC);
		OPPLAYER4 = new PacketDef(ObfuscatedNames.OPPLAYER4, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","ca");
				put("playerIndex","dz");
			}
		}, PacketType.OPPLAYER);
		OPNPC5 = new PacketDef(ObfuscatedNames.OPNPC5, new LinkedHashMap<String,String>(){
			{
				put("npcIndex","dr");
				put("ctrlDown","aw");
			}
		}, PacketType.OPNPC);
		OPPLAYER5 = new PacketDef(ObfuscatedNames.OPPLAYER5, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","dd");
				put("ctrlDown","aw");
			}
		}, PacketType.OPPLAYER);
		OPLOC5 = new PacketDef(ObfuscatedNames.OPLOC5, new LinkedHashMap<String,String>(){
			{
				put("worldPointX","dr");
				put("objectId","dz");
				put("ctrlDown","aw");
				put("worldPointY","dr");
			}
		}, PacketType.OPLOC);
		OPPLAYER1 = new PacketDef(ObfuscatedNames.OPPLAYER1, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","au");
				put("ctrlDown","aw");
			}
		}, PacketType.OPPLAYER);
		MOVE_GAMECLICK = new PacketDef(ObfuscatedNames.MOVE_GAMECLICK, new LinkedHashMap<String,String>(){
			{
				put("5","aw");
				put("ctrlDown","aw");
				put("worldPointX","au");
				put("worldPointY","dd");
			}
		}, PacketType.MOVE_GAMECLICK);
		IF_BUTTON1 = new PacketDef(ObfuscatedNames.IF_BUTTON1, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		IF_BUTTON3 = new PacketDef(ObfuscatedNames.IF_BUTTON3, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		IF_BUTTON2 = new PacketDef(ObfuscatedNames.IF_BUTTON2, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
		EVENT_MOUSE_CLICK = new PacketDef(ObfuscatedNames.EVENT_MOUSE_CLICK, new LinkedHashMap<String,String>(){
			{
				put("mouseInfo","au");
				put("x","au");
				put("y","au");
			}
		}, PacketType.EVENT_MOUSE_CLICK);
		IF_BUTTON10 = new PacketDef(ObfuscatedNames.IF_BUTTON10, new LinkedHashMap<String,String>(){
			{
				put("widgetId","as");
				put("childId","au");
				put("itemId","au");
			}
		}, PacketType.IF_BUTTON);
	}
}
