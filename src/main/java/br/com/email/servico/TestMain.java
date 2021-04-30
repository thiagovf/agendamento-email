package br.com.email.servico;

public class TestMain {
	public static void main(String[] args) {
		String valueAsString = "01234567890123456789";
		String formated = String.format("%s-%s.%s.%s.%s.%s", 
				valueAsString.substring(0, 7), valueAsString.substring(7, 9), 
				valueAsString.substring(9, 13), valueAsString.substring(13, 14),
				valueAsString.substring(14, 16), valueAsString.substring(16, 20));
		System.out.println(formated);
	}
}