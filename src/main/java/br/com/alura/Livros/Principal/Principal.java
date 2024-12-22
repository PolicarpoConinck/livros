package br.com.alura.Livros.Principal;

import br.com.alura.Livros.ConsumoAPI.ConsumoAPI;
import br.com.alura.Livros.ConsumoAPI.ConverteDados;
import br.com.alura.Livros.ConsumoAPI.RespostaAPI;
import br.com.alura.Livros.Model.Autor;
import br.com.alura.Livros.Model.DadosLivro;
import br.com.alura.Livros.Model.Livro;
import br.com.alura.Livros.Repository.AutorRepository;
import br.com.alura.Livros.Repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class Principal {
    private final String ENDERECO = "https://gutendex.com/books/?search=";
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private Scanner leitura = new Scanner(System.in);
    private List<DadosLivro> dadosSeries = new ArrayList<>();

    @Autowired
    private LivroRepository livroRepository;
    @Autowired
    private AutorRepository autorRepository;

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1  - Buscar Livro pelo título
                    2  - Listar livros registrados
                    3  - Listar autores registrados
                    4  - Listar autores vivos em um determinado ano
                    5  - Listar livros em um determinado idíoma
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarLivro();
                    break;
                case 2:
                    ListarLivrosRegistrados();
                    break;
                case 3:
                    listarAutoresResgistados();
                    break;
                case 4:
                    listarAutoresVivosPorAno();
                    break;
                case 5:
                    listarLivrosPorIdiona();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarLivro() {
        System.out.println("Digite o nome do livro para busca:");
        String nomeLivro = leitura.nextLine();

        var livroExistente = livroRepository.findByTituloContainingIgnoreCase(nomeLivro);
        if (livroExistente.isPresent()) {
            System.out.println("Livro encontrado no banco de dados:");
            System.out.println(livroExistente.get());
            return;
        }


        DadosLivro dados = getDadosLivro(nomeLivro);
        if (dados == null) {
            System.out.println("Livro não encontrado!!!");
            return;
        }

        salvarLivro(dados);
        System.out.println("Livro salvo no banco de dados.");
        System.out.println(dados);
    }

    private DadosLivro getDadosLivro(String nomeLivro) {
        String enderecoBusca = ENDERECO + nomeLivro.replace(" ", "+");
        System.out.println(enderecoBusca);

        var json = consumo.obterDados(enderecoBusca);


        if (json.contains("{\"count\":0,\"next\":null,\"previous\":null,\"results\":[]}")) {
            return null;
        } else {

            RespostaAPI resposta = conversor.obterDados(json, RespostaAPI.class);


            List<DadosLivro> livros = resposta.getResults();

            if (!livros.isEmpty()) {
                DadosLivro dados = livros.get(0);
                return dados;
            } else {
                return null;
            }
        }
    }

    private void salvarLivro(DadosLivro dadosLivro) {
        Livro livro = new Livro();
        livro.setTitulo(dadosLivro.titulo());
        livro.setDownloads(dadosLivro.downloads());
        livro.setLingua(dadosLivro.idiomas().get(0));

        System.out.println(dadosLivro.autores().get(0).name());
        Autor autor ;
        var autorExistente = autorRepository.findByNome(dadosLivro.autores().get(0).name());

        if (autorExistente.isPresent()) {
            autor = autorExistente.get();

        } else {
            autor = new Autor();
            autor.setNome(dadosLivro.autores().get(0).name());
            autor.setAnoNascimento(dadosLivro.autores().get(0).birthYear());
            autor.setAnoMorte(dadosLivro.autores().get(0).deathYear());
            autorRepository.save(autor);

       }
        livro.setAutor(autor);
        livroRepository.save(livro);
    }

        private void ListarLivrosRegistrados () {
            var livros = livroRepository.findAll();
            livros.forEach(System.out::println);
        }

        private void listarAutoresResgistados () {
            var autores = autorRepository.findAll();
            autores.forEach(System.out::println);
        }
        private void listarAutoresVivosPorAno() {
            System.out.println("Informe o ano que  deseja saber se o autor esta vivo :");
            var ano = leitura.nextInt();
            List<Autor> autorvivo =  autorRepository.findByAnoMorteGreaterThan(ano);
            autorvivo.forEach(System.out::println);
        }

    private void listarLivrosPorIdiona() {
        System.out.println("Informe o idioma que deseja listar os livros  Português, Inglês ou Espanhol :");
        var lingua = leitura.nextLine();
       if (lingua.equals("Português")){
           lingua = "pt";
       } else if(lingua.equals("Inglês")) {
           lingua = "en";
       } else if(lingua.equals("Espanhol")){
            lingua = "es";
        } else {
           System.out.println("Lingua não definida!!!");
           return;
       }
        List<Livro> livroPorlingua = livroRepository.livrosPorIdioma(lingua);
        livroPorlingua.forEach(System.out::println);

    }

}
