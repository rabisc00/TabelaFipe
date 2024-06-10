package br.com.alura.TabelaFipe.principal;

import br.com.alura.TabelaFipe.model.*;
import br.com.alura.TabelaFipe.service.ConsumoApi;
import br.com.alura.TabelaFipe.service.ConverteDados;

import java.util.*;

public class Principal {
    private Scanner leitor = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    public void exibeMenu() {
        System.out.println("Qual veiculo você quer: carro, moto, ou caminhão?");

        String respostaVeiculo = leitor.nextLine().toLowerCase();

        if (respostaVeiculo.startsWith("car")) {
            respostaVeiculo = "carros";
        } else if (respostaVeiculo.startsWith("mot")) {
            respostaVeiculo = "motos";
        } else if (respostaVeiculo.startsWith("cam")) {
            respostaVeiculo = "caminhoes";
        } else {
            throw new RuntimeException("Opção inválida.");
        }

        String urlVeiculos = "https://parallelum.com.br/fipe/api/v1/" + respostaVeiculo + "/marcas/";

        String veiculos = consumoApi.obterDados(urlVeiculos);
        List<Dados> marcasVeiculo = conversor.obterLista(veiculos, Dados.class);

        System.out.println("\nTodas as marcas do veículo escolhido: ");

        marcasVeiculo.stream().sorted(Comparator.comparing(Dados::codigo))
                .forEach(m -> System.out.println("Código: " + m.codigo() + ", Marca: " + m.nome()));

        System.out.println("\nQual código você deseja consultar?");
        String respostaMarca = leitor.nextLine();

        Optional<Dados> marcaEncontrada = marcasVeiculo.stream().filter(m -> m.codigo().equals(respostaMarca)).findFirst();
        if (marcaEncontrada.isEmpty()) {
            throw new RuntimeException("Código inválido");
        }

        String urlModelos = urlVeiculos + respostaMarca + "/modelos/";

        String modelos = consumoApi.obterDados(urlModelos);
        DadosModelosMarca modelosMarca = conversor.obterDados(modelos, DadosModelosMarca.class);
        List<Dados> modelosSorted = modelosMarca.modelos().stream()
                .sorted(Comparator.comparing(Dados::codigo))
                .toList();

        System.out.println("\nModelos da marca escolhida:");

        modelosSorted.forEach(m -> System.out.println("Código: " + m.codigo() + ", Modelo: " + m.nome()));

        System.out.println("\nDigite um trecho do nome do veículo para consulta: ");

        String trechoModelo = leitor.nextLine();

        List<Dados> modelosFiltered = modelosSorted.stream()
                .filter(m -> m.nome().toLowerCase().contains(trechoModelo.toLowerCase()))
                .toList();

        if (modelosFiltered.isEmpty()) {
            throw new RuntimeException("Não foi encontrado nenhum modelo com o trecho digitado");
        }

        System.out.println("\nModelos com o trecho digitado:");
        modelosFiltered.forEach(m -> System.out.println("Código: " + m.codigo() + ", Modelo: " + m.nome()));

        System.out.println("\nQual código você deseja consultar?");

        String codigoModelo = leitor.nextLine();

        Optional<Dados> modeloEncontrado = modelosFiltered.stream()
                .filter(m -> m.codigo().contains(codigoModelo))
                .findFirst();

        if (modeloEncontrado.isEmpty()) {
            throw new RuntimeException("Código inválido");
        }

        String urlAnosModelo = urlModelos + codigoModelo + "/anos/";

        String todosAnosModelos = consumoApi.obterDados(urlAnosModelo);
        List<Dados> anosModelo = conversor.obterLista(todosAnosModelos, Dados.class);

        List<DadosAno> todosAnos = new ArrayList<>();

        for (Dados m : anosModelo) {
            String urlAnos = urlAnosModelo + m.codigo();

            String anosJson = consumoApi.obterDados(urlAnos);
            DadosAno dadosAno = conversor.obterDados(anosJson, DadosAno.class);
            todosAnos.add(dadosAno);
        }

        System.out.println("\nValores de todos os anos do modelo:");

        todosAnos.stream()
                .sorted(Comparator.comparing(DadosAno::anoModelo))
                .forEach(System.out::println);
    }
}
