public class ConvertStringParameterToInt {

	private int pixelPerBlockX;
	private int pixelPerBlockY;
	private double threshold;
	
	public static void main(String[] args) {
		ConvertStringParameterToInt c = new ConvertStringParameterToInt();
		c.convertStringParameterToInt("10", "20", "0.3");
		System.out.println("arg[0]: " + c.pixelPerBlockX);
		System.out.println("arg[1]: " + c.pixelPerBlockY);
		System.out.println("arg[2]: " + c.threshold);
	}
	
	public void convertStringParameterToInt(String... args) {
		this.pixelPerBlockX = Integer.parseInt(args[0]);
		this.pixelPerBlockY = Integer.parseInt(args[1]);
		this.threshold = Double.parseDouble(args[2]);
	}
}
