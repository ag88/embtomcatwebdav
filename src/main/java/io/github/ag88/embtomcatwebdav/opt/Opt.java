package io.github.ag88.embtomcatwebdav.opt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import io.github.ag88.embtomcatwebdav.WebDavServer;

// TODO: Auto-generated Javadoc
/**
 * The Class Prop.
 */
public class Opt implements Comparable<Opt> {
	
	/**
	 * The Enum PropType.
	 */
	public enum PropType {
		
		/** normal property, for both CLI and properties file*/
		Norm,
		
		/** CLI only property*/
		CLI,
		
		/** Properties file only property */
		Prop
	}
	
	/** The name. */
	protected String name;
	
	/** The description. */
	protected String description;
	
	/** The opt, single char. */
	protected String opt;
	
	/** The longopt for cmd line. */
	protected String longopt;
	
	/** The argname for cmd line. */
	protected String argname;
	
	/** The valclazz. */
	protected Class valclazz = String.class;
	
	/** The value. */
	protected Object value = null;
	
	/** The defaultval. */
	protected Object defaultval;
	
	/** password processing flag */
	protected boolean passwd = false;
	
	/** The property type */
	protected PropType type = PropType.Norm;
	
	/** The process command line options. */
	protected boolean cmdproc = false;
		
	/** Has args. */
	protected boolean hasarg = true;
	
	
	/** validate flag, true = validation on */
	protected boolean validate = false;
	
	/** replace flag, true = validation on */
	protected boolean replace = false;	
	
	/** The priority. lower is higher prority*/
	protected int priority = 50;
	
	
	/**
	 * Instantiates a new prop.
	 */
	public Opt() {
	}	



	/**
	 * Instantiates a new opt.
	 *
	 * @param name the name
	 * @param description the description
	 * @param opt the opt
	 * @param longopt the longopt
	 * @param argname the argname
	 * @param valclazz the valclazz
	 * @param value the value
	 * @param defaultval the defaultval
	 * @param passwd the passwd
	 * @param type the type
	 * @param cmdproc the cmdproc
	 * @param hasarg the hasarg
	 * @param priority the priority
	 */
	public Opt(String name, String description, String opt, String longopt, String argname, Class valclazz,
			Object value, Object defaultval, boolean passwd, PropType type, boolean cmdproc, boolean hasarg,
			int priority) {
		super();
		this.name = name;
		this.description = description;
		this.opt = opt;
		this.longopt = longopt;
		this.argname = argname;
		this.valclazz = valclazz;
		this.value = value;
		this.defaultval = defaultval;
		this.passwd = passwd;
		this.type = type;
		this.cmdproc = cmdproc;
		this.hasarg = hasarg;
		this.priority = priority;
	}


	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the opt.
	 *
	 * @return the opt
	 */
	public String getOpt() {
		return opt;
	}

	/**
	 * Sets the opt.
	 *
	 * @param opt the new opt
	 */
	public void setOpt(String opt) {
		this.opt = opt;
	}

	/**
	 * Gets the longopt.
	 *
	 * @return the longopt
	 */
	public String getLongopt() {
		return longopt;
	}

	/**
	 * Sets the longopt.
	 *
	 * @param longopt the new longopt
	 */
	public void setLongopt(String longopt) {
		this.longopt = longopt;
	}

	/**
	 * Gets the argname.
	 *
	 * @return the argname
	 */
	public String getArgname() {
		return argname;
	}

	/**
	 * Sets the argname.
	 *
	 * @param argname the new argname
	 */
	public void setArgname(String argname) {
		this.argname = argname;
	}

	/**
	 * Gets the valclazz.
	 *
	 * @return the valclazz
	 */
	public Class getValclass() {
		return valclazz;
	}

	/**
	 * Sets the valclazz.
	 *
	 * @param valclazz the new valclazz
	 */
	public void setValclass(Class valclazz) {
		this.valclazz = valclazz;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Gets the defaultval.
	 *
	 * @return the defaultval
	 */
	public Object getDefaultval() {
		return defaultval;
	}

	/**
	 * Sets the defaultval.
	 *
	 * @param defaultval the new defaultval
	 */
	public void setDefaultval(Object defaultval) {
		this.defaultval = defaultval;
	}

	/**
	 * Checks if is passwd.
	 *
	 * @return true, if is passwd
	 */
	public boolean isPasswd() {
		return passwd;
	}

	/**
	 * Sets the passwd.
	 *
	 * @param passwd the new passwd
	 */
	public void setPasswd(boolean passwd) {
		this.passwd = passwd;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public PropType getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(PropType type) {
		this.type = type;
	}

	/**
	 * Checks if is cmdproc.
	 *
	 * @return true, if is cmdproc
	 */
	public boolean isCmdproc() {
		return cmdproc;
	}

	/**
	 * Sets the cmdproc.
	 *
	 * @param cmdproc the new cmdproc
	 */
	public void setCmdproc(boolean cmdproc) {
		this.cmdproc = cmdproc;
	}

	
	/**
	 * Checks if it hasarg.
	 *
	 * @return true, if is hasarg
	 */
	public boolean isHasarg() {
		return hasarg;
	}


	/**
	 * Sets hasarg.
	 *
	 * @param hasarg the new hasarg
	 */
	public void setHasarg(boolean hasarg) {
		this.hasarg = hasarg;
	}

	
	/**
	 * Checks if is validate.
	 *
	 * if isvalidate is true, {@link #isvalid(Object)} would be run
	 * at when value is provided
	 * 
	 * @return true, if is validate
	 */
	public boolean isValidate() {
		return validate;
	}

	/**
	 * Sets the validate.
	 * 
	 * if isvalidate is true, {@link #isvalid(Object)} would be run
	 * at when value is provided
	 * 
	 * @param validate the new validate
	 */
	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	/**
	 * Gets the priority.
	 *
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}


	/**
	 * Sets the priority.
	 *
	 * @param priority the new priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

		
	/**
	 * Checks if is replace.
	 *
	 * @return true, if is replace
	 */
	public boolean isReplace() {
		return replace;
	}



	/**
	 * Sets the replace flag
	 * 
	 * if true, 
	 *
	 * @param replace the new replace flag
	 */
	public void setReplace(boolean replace) {
		this.replace = replace;
	}


	/**
	 * methods to be override
	 */
	
	/**
	 * Gets the command line option, when cmdproc is set
	 *
	 * @return the option
	 */
	public Option getOption() {
		return null;
	}
			
	/**
	 * Process the command line options, when cmdproc is set
	 *
	 * @param cmd the cmd
	 */
	public void process(CommandLine cmd, Object... objects ) {		
	}
		
	
	/**
	 * Checks if is valid.
	 *
	 * @param object the object
	 * @return true, if is valid
	 */
	public boolean isvalid(Object object) {
		return true;
	}
	
	/**
	 * Replace.
	 *
	 * @param object the object
	 * @return the replaced object 
	 */
	public Object replace(Object object) {
		return null;		
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		
		sb.append("priority: ");
		sb.append(priority);
		sb.append(", name: ");
		sb.append(name);
		sb.append(", desc: ");
		sb.append(description == null ? "null" : description);
		sb.append(", opt: ");
		sb.append(opt == null ? "null" : opt);
		sb.append(", longopt: ");
		sb.append(longopt == null ? "null" : longopt);
		sb.append(", hasarg: ");
		sb.append(Boolean.toString(hasarg));
		sb.append(", argname: ");
		sb.append(argname == null ? "null" : argname);
		sb.append(", valclass: ");
		sb.append(valclazz == null ? "null" : valclazz.getSimpleName());
		sb.append(", value: ");
		sb.append(value == null ? "null" : value);
		sb.append(", defaultval: ");
		sb.append(defaultval == null ? "null" : defaultval);
		sb.append(", ispasswd: ");
		sb.append(Boolean.toString(passwd));
		sb.append(", PropType: ");
		sb.append(type.name());
		sb.append(", iscmdproc: ");
		sb.append(Boolean.toString(cmdproc));
		
		return sb.toString();
	}

	
	/**
	 * Compare to, order by priority, then name.
	 *
	 * @param o the other object
	 * @return the int
	 */
	@Override
	public int compareTo(Opt o) {
		if(Integer.valueOf(priority).equals(o.getPriority()))
			return name.compareTo(o.getName());
		else 
			return Integer.valueOf(priority).compareTo(o.getPriority());
	}






	
}
