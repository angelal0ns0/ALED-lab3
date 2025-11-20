package es.upm.dit.aled.lab3.binary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.upm.dit.aled.lab3.FASTAReader;

/**
 * Reads a FASTA file containing genetic information and allows for the search
 * of specific patterns within these data. The information is stored as an array
 * of bytes that contain nucleotides in the FASTA format. Since this array is
 * usually created before knowing how many characters in the origin FASTA file
 * are valid, an int indicating how many bytes of the array are valid is also
 * stored. All valid characters will be at the beginning of the array.
 * 
 * This extension of the FASTAReader uses a sorted dictionary of suffixes to
 * allow for the implementation of binary search.
 * 
 * @author mmiguel, rgarciacarmona
 *
 */
public class FASTAReaderSuffixes extends FASTAReader {
	protected Suffix[] suffixes;

	/**
	 * Creates a new FASTAReader from a FASTA file.
	 * 
	 * At the end of the constructor, the data is sorted through an array of
	 * suffixes.
	 * 
	 * @param fileName The name of the FASTA file.
	 */
	public FASTAReaderSuffixes(String fileName) {
		//llama al constructor de FastaReader(padre) para leer el genoma del acrhivo
		super(fileName); 
		this.suffixes = new Suffix[validBytes]; //crea array vacio del mismo tamaño que validBytes
		for (int i = 0; i < validBytes; i++) //recorre suffixes creando un new suffix con los numeros correspondientes
			suffixes[i] = new Suffix(i); // creando un new suffix con los numeros correspondientes
		// Sorts the data
		sort(); //metodo sort programado aparte--> ordena el array de sujijos
	}

	/*
	 * Helper method that creates a array of integers that contains the positions of
	 * all suffixes, sorted alphabetically by the suffix.
	 */
	private void sort() {
		// Instantiate the external SuffixComparator, passing 'this' (the reader)
		// so it can access the content and validBytes fields.
		SuffixComparator suffixComparator = new SuffixComparator(this); //invoca al comparador de sufijos
		// Use the external Comparator for sorting.
		Arrays.sort(this.suffixes, suffixComparator); //lo que quiere ordenar
	}

	/**
	 * Prints a list of all the suffixes and their position in the data array.
	 */
	public void printSuffixes() {
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Index | Sequence");
		System.out.println("-------------------------------------------------------------------------");
		for (int i = 0; i < suffixes.length; i++) {
			int index = suffixes[i].suffixIndex;
			String ith = "\"" + new String(content, index, Math.min(50, validBytes - index)) + "\"";
			System.out.printf("  %3d | %s\n", index, ith);
		}
		System.out.println("-------------------------------------------------------------------------");
	}

	/**
	 * Implements a binary search to look for the provided pattern in the data
	 * array. Returns a List of Integers that point to the initial positions of all
	 * the occurrences of the pattern in the data.
	 * 
	 * @param pattern The pattern to be found.
	 * @return All the positions of the first character of every occurrence of the
	 *         pattern in the data.
	 */
	@Override
	public List<Integer> search(byte[] pattern) {
		List <Integer> resultados = new ArrayList<Integer>(); //lista que devolveré
		int lo = 0; //posicc 0 (de suffixes)
		int hi = suffixes.length; //tamaño de suffixes
		boolean found = false; 
		int index = 0;
		
		while(!found && hi - lo >1) {
			int m = (lo + hi) / 2;
			int posSuffix = this.suffixes[m].suffixIndex; //.suffixIndex --> le pido el indice "interno" 
														//del obejto suffix en la posicion m de suffixes
			
			while(index < pattern.length && posSuffix +index < content.length && pattern[index]==content[posSuffix + index])
				index++; //contaremos coincidencias--> index++
			if(index==pattern.length) { 
				//index se ha incrementado hasta ser del tamaño del patron (hemos encontrado el patron entero)
				resultados.add(posSuffix);
				found = true;
			} //cierre if
			else { //no hay coincidencia completa
				if(pattern[index] < content[posSuffix + index])
					hi = m--; //me voy hacia la izquierda en el lim superior
				else
					lo = m++; //me quedo con la mitad derecha, lim inferior en posicion m+1 = m++
				index = 0; //reiniciamos la busqueda en la mitad que sea, hay que poner el index a cero		
			}
			
		}  
		return resultados;
	}

	public static void main(String[] args) {
		long t1 = System.nanoTime();
		FASTAReaderSuffixes reader = new FASTAReaderSuffixes(args[0]);
		if (args.length == 1)
			return;
		byte[] patron = args[1].getBytes();
		System.out.println("Tiempo de apertura de fichero: " + (System.nanoTime() - t1));
		long t2 = System.nanoTime();
		System.out.println("Tiempo de ordenación: " + (System.nanoTime() - t2));
		reader.printSuffixes();
		long t3 = System.nanoTime();
		List<Integer> posiciones = reader.search(patron);
		System.out.println("Tiempo de búsqueda: " + (System.nanoTime() - t3));
		if (posiciones.size() > 0) {
			for (Integer pos : posiciones)
				System.out.println("Encontrado " + args[1] + " en " + pos);
		} else
			System.out.println("No he encontrado " + args[1] + " en ningún sitio.");
		System.out.println("Tiempo total: " + (System.nanoTime() - t1));
	}
}
