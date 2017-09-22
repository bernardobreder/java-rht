package rht;

/**
 * Classe que gerencia a numeração de acordo com os níveis
 * 
 * @author bernardobreder
 * 
 */
public class NumberOfLevel {

	/** Níveis */
	private final int levels;

	private final int[] array;

	/**
	 * Construtor
	 * 
	 * @param levels
	 */
	public NumberOfLevel() {
		this.levels = 5;
		this.array = new int[5];
		for (int n = levels - 1; n >= 0; n--) {
			this.array[n] = 0;
		}
	}

	/**
	 * Retorna a numeração em função do nível
	 * 
	 * @param level
	 * @return numeração em função do nível
	 */
	public String toString(int level) {
		if (level <= 0 || level >= this.levels) {
			return "";
		}
		int index = level - 1;
		this.array[index]++;
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < this.array.length; n++) {
			int i = this.array[n];
			if (i == 0) {
				break;
			}
			sb.append(i);
			sb.append('.');
		}
		return sb.toString();
	}

	/**
	 * @return the levels
	 */
	public int getLevels() {
		return levels;
	}

}
