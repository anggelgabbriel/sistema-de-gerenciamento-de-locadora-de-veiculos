# Sistema de Gerenciamento de Locadora de Veículos

O Vértice é um aplicativo desktop em Java e Swing para gerenciar veículos, clientes, funcionários, reservas, aluguéis e pagamentos. Os registros são armazenados em SQLite.

Projeto da disciplina de Programação Orientada a Objetos (PEX0130) da UFERSA. A implementação parte de um diagrama de classes UML e acompanha o fluxo da reserva do veículo até a devolução e o pagamento.

## Como abrir

Tenha o Java 8 ou superior instalado e mantenha a pasta `lib` ao lado de `Vertice.jar`. Ela contém o driver SQLite usado pelo aplicativo.

- **Windows** — dê dois cliques em `Abrir Vertice.cmd`.
- **Linux** — abra o terminal na pasta do projeto e execute `sh "./Abrir Vertice.sh"`.

Você também pode abrir o aplicativo pelo terminal nos dois sistemas com o mesmo comando.

```text
java -jar Vertice.jar
```

Na primeira execução, o aplicativo cria `sistema.db` na pasta a partir da qual foi iniciado e carrega cadastros fictícios, um aluguel ativo com saldo e uma reserva confirmada. Para criar o banco vazio, execute `java -jar Vertice.jar --vazio` na primeira vez. Se o banco já existe, o aplicativo abre os registros salvos nele.

No primeiro acesso, escolha um usuário e uma senha de pelo menos oito caracteres. Use os mesmos dados nas próximas execuções. A senha é armazenada como hash com salt na tabela `acesso`.

Cada operação concluída é salva no banco, e os dados continuam disponíveis ao reabrir o aplicativo. O registro de pagamento é uma simulação local.

## Banco de dados

O projeto segue o [exemplo Java + SQLite do professor](https://github.com/ruanpabllobc/modelo-poo-sqlite). Os comandos `CREATE TABLE` ficam em [Banco.java](src/locadora/database/Banco.java). As consultas e gravações ficam em [PersistenciaLocadora.java](src/locadora/modelo/PersistenciaLocadora.java).

O banco tem seis tabelas da locadora (`carro`, `cliente`, `funcionario`, `reserva`, `aluguel` e `pagamento`) e uma tabela de login (`acesso`). As datas são armazenadas em formato ISO e os valores monetários são gravados como texto decimal para preservar os centavos.

Para ver as tabelas no Beekeeper Studio, crie uma conexão **SQLite** e escolha o arquivo `sistema.db`. O banco local fica fora do Git.

## Experimente o fluxo

1. Em **Reservas**, selecione uma reserva confirmada para hoje e clique em **Retirar veículo**. Se a reserva de exemplo estiver vencida, solicite e confirme uma nova. A retirada cria um registro em **Aluguéis**.
2. Selecione o aluguel recém-criado, clique em **Registrar devolução** e confirme a data.
3. Selecione o mesmo aluguel e clique em **Receber pagamento**. O formulário sugere o saldo em aberto, mas aceita pagamentos parciais.
4. Depois de quitar o saldo, selecione o aluguel e clique em **Finalizar**.

Você também pode cadastrar e editar veículos, clientes e funcionários, cancelar reservas, abrir aluguéis sem reserva e estornar pagamentos antes da finalização. A agenda impede que dois compromissos confirmados ocupem o mesmo veículo nas mesmas datas.

A cobrança inclui a primeira e a última data do período contratado. Uma devolução antecipada mantém o preço contratado, enquanto uma devolução após o prazo acrescenta diárias. CPF e CNH são validados pelo formato; o programa não consulta cadastros oficiais.

## Código

- `src/locadora/modelo`: as seis classes do diagrama. `Pessoa` concentra os campos comuns de cliente e funcionário, e `Locadora` coordena operações como a confirmação de reservas e a abertura de aluguéis.
- `src/locadora/ui`: janela, tabelas, formulários e tema visual em Swing.
- `src/locadora/Main.java`: ponto de entrada.

Os estados usam `enum` para evitar valores inválidos. Os valores em reais usam `BigDecimal` para representar centavos com precisão. O total e o saldo do aluguel são calculados a partir das datas, da diária e dos pagamentos confirmados.

No Windows, abra o PowerShell na pasta do projeto e rode este comando para recompilar com um JDK.

```powershell
powershell -ExecutionPolicy Bypass -File .\compilar.ps1
```

O script procura o compilador em `JAVA_HOME`, no `PATH` ou na extensão Java do VS Code. O JAR resultante é compatível com Java 8.
