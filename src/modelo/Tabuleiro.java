package modelo;

import java.util.ArrayList;
//import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tabuleiro implements CampoObservador {

	private final int linha;
	private final int coluna;
	private final int mina;
	
	private final List<Campo> campos = new ArrayList<>();
	private final List<Consumer<ResultadoEvento>> observadores = new ArrayList<>();

	public Tabuleiro(int linha, int coluna, int mina) {
		this.linha = linha;
		this.coluna = coluna;
		this.mina = mina;
		
		gerarCampos();
		associarOsVizinhos();
		sortearMinas();
	}
	
	public void paraCada(Consumer<Campo> funcao) {
		campos.forEach(funcao);
	}
	
	public void registrarObservador(Consumer<ResultadoEvento> observador) {
		observadores.add(observador);
	}
	
	public void notificarObservadores(Boolean resultado) {
		observadores.stream().forEach(obs -> obs.accept(new ResultadoEvento(resultado)));
	}
	
	public void abrir(int linha, int coluna) {
			campos.parallelStream()
			.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
			.findFirst().ifPresent(c -> c.abrir());
	}
	
	private void gerarCampos() {
		for (int l = 0; l < linha; l++) {
			for (int c = 0; c < coluna; c++) {
				Campo campo = new Campo(l, c);
				campo.registrarObservador(this);
				campos.add(campo);
			}
		}
	}
	
	private void associarOsVizinhos() {
		for(Campo c1: campos) {
			for(Campo c2: campos) {
				c1.adicionarVizinhos(c2);
				}
		}
	}
	
	private void sortearMinas() {
		long minasArmadas = 0;
		Predicate<Campo> minado = c -> c.isMinado();
		do {
			int aleatorio = (int) (Math.random() * campos.size());
			campos.get(aleatorio).minar();
			minasArmadas = campos.stream().filter(minado).count();
		}while(minasArmadas < mina);
			
		}
	
	public boolean objetivoAlcancado() {
		return campos.stream().allMatch(c -> c.objetivoAlcancado());
	}
	
	public void reiniciar() {
		campos.stream().forEach(c -> c.reiniciar());
		sortearMinas();
	}
		
	public int getLinha() {
		return linha;
	}

	public int getColuna() {
		return coluna;
	}

	@Override
	public void eventoOcorreu(Campo campo, CampoEvento evento) {
		if(evento == CampoEvento.EXPLODIR) {
			mostrarMinas(); 
			notificarObservadores(false);
		}else if(objetivoAlcancado()){
		 notificarObservadores(true);
		}
	}
	
	private void mostrarMinas() {
		campos.stream()
		.filter(c -> c.isMinado())
		.filter(c -> !c.isMarcado())
		.forEach(c -> c.setAberto(true));
	}

	
	public void alternarMarcacao(int linha, int coluna) {
		campos.stream().filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
		.findFirst().ifPresent(c -> c.alternarMarcacao());
	}
	
}
