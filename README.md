# Rota de Viagem #

Um turista deseja viajar pelo mundo pagando o menor preço possível independentemente do número de conexões necessárias.
Vamos construir um programa que facilite ao nosso turista, escolher a melhor rota para sua viagem.

Para isso precisamos inserir as rotas através de um arquivo de entrada.

## Input Example ##
```csv
GRU,BRC,10
BRC,SCL,5
GRU,CDG,75
GRU,SCL,20
GRU,ORL,56
ORL,CDG,5
SCL,ORL,20
```

## Explicando ## 
Caso desejemos viajar de **GRU** para **CDG** existem as seguintes rotas:

1. GRU - BRC - SCL - ORL - CDG ao custo de **$40**
2. GRU - ORL - CDG ao custo de **$61**
3. GRU - CDG ao custo de **$75**
4. GRU - SCL - ORL - CDG ao custo de **45**

O melhor preço é da rota **1** logo, o output da consulta deve ser **GRU - BRC - SCL - ORL - CDG**.

### Desafio técnico ###

Duas interfaces de consulta devem ser implementadas:
- Interface de console deverá receber um input com a rota no formato "DE-PARA" e imprimir a melhor rota e seu respectivo valor.
  Exemplo:
  ```shell
  please enter the route: GRU-CGD
  best route: GRU - BRC - SCL - ORL - CDG > $40
  please enter the route: BRC-CDG
  best route: BRC - ORL > $30
  ```

- Interface Rest
    A interface Rest deverá suportar:
    - Registro de novas rotas. Essas novas rotas devem ser persistidas no arquivo csv utilizado como input(input-routes.csv),
    - Consulta de melhor rota entre dois pontos.

### Execução do programa ###
A inicializacao do teste se dará por linha de comando onde o primeiro argumento é o arquivo com a lista de rotas inicial.

Obs.: Baixar o projeto do repositório Github (https://github.com/luisalexandre19/challenge_bexs), via GIT ou Download do Zip.

Executar um dos comandos abaixo, de acordo com o Sistema Operacional.

```shell for linux/macOS
$ ./gradlew bootRun -q --console=plain --args='/home/luis-santos/challenge/input-routes.csv'
```

```shell for windows
$ gradlew.bat bootRun -q --console=plain --args='/home/luis-santos/challenge/input-routes.csv'
```

Para executar os testes unitários

```shell for linux/macOS
$ ./gradlew test --info
```

```shell for windows
$ gradlew.bat test
```

Duas interfaces de consulta devem ser implementadas:
- Interface de console deverá receber um input com a rota no formato "DE-PARA" e imprimir a melhor rota e seu respectivo valor.
  Exemplo:
  ```shell
  please enter the route: GRU-CGD
  best route: GRU - BRC - SCL - ORL - CDG > $40
  please enter the route: BRC-CDG
  best route: BRC - ORL > $30
  ```

- Interface Rest
    A interface Rest deverá suportar:
    - Registro de novas rotas. Essas novas rotas devem ser persistidas no arquivo csv utilizado como input(input-routes.csv),
    - Consulta de melhor rota entre dois pontos.

## Estrutura dos arquivos/pacotes ##

* configs       -> Configurações do framework (Spring Batch)
* entities      -> Entidades para persistência
* exceptions    -> Componentes para controle das possíveis exceções 
* models.dtos   -> Modelos para comunicação com API Rest.
* respositories -> Interface para persistência
* resources.v1  -> Endpoints para API Rest
* services      -> Componentes para regras de negócio 

## Solução ##

Nesta POC foi utilizado o algoritmo dijkstra (vide referências), para encontrar menor caminho, que no caso seria a melhor rota para viagem.

Logo, foi utilizado o ecosistema Spring que nos permitiu unir duas interfaces, console e Rest, além de trazer uma economia de código e redução de verbosidade.

* Dependências 
    * SpringBoot
    * SpringBatch (Job - Carregamento do arquivo de entrada)
    * SpringWeb (Api Rest)
    * SpringJpa (Persistência na base em memória H2)
    
Ao iniciar a solução, inicia-se também um servidor de aplicação na porta 8080, além da interface console, conforme solicitado pelo desafio. 
No LOG gerado é possível visualizar, que arquivo CSV (caminho completo) fornecido como argumento, será carregado antes da liberação da iteração com o usuário em uma base de dados em memória (carga inicial de rotas).
Entretanto, esta solução flexibiliza esta carga inicial e continua seu fluxo, independente do êxito do carregamento, porém é obrigatório a passagem do arquivo!
Nesta também é possível utilizar simultaneamente as duas interfaces, Rest e console em um único Boot.

Simples Doc da API Rest.

* Criar novas Rotas
    * URL: http://localhost:8080/flights/v1/routes
    * METHOD: POST 
    * REPONSE: CREATED (201)
    * PAYLOAD: (JSON)
    ```
    {
        "source" : "GRU",
        "target" : "SLG",
        "cost": 1.1
    }
    ```
  
* Encontrar melhor rota
    * URL: http://localhost:8080/flights/v1/routes/source/{source}/target/{target}
    * METHOD: GET 
    * REPONSE: OK (200)
    * PATHPARAM: {source} = GRU e {target} = CDG
    * RESPONSE: (JSON)
    ```
    {
        "path": "GRU - BRC - SCL - ORL - CDG",
        "cost": 40.0
    }
    ```
  
## Referências ##

* https://pt.wikipedia.org/wiki/Algoritmo_de_Dijkstra
* https://www.baeldung.com/java-dijkstra
* https://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html