# Sistema de Gerenciamento de Locadora de Veículos

O Vértice é um aplicativo desktop em Java e Swing para gerenciar veículos, clientes, funcionários, reservas, aluguéis e pagamentos. Não precisa de Maven, Gradle nem bibliotecas externas.

Projeto da disciplina de Programação Orientada a Objetos (PEX0130) da UFERSA. A implementação parte de um diagrama de classes UML e acompanha o fluxo da reserva do veículo até a devolução e o pagamento.

## Como abrir

Tenha o Java 8 ou superior instalado.

- **Windows** — dê dois cliques em `Abrir Vertice.cmd`.
- **Linux** — abra o terminal na pasta do projeto e execute `sh "./Abrir Vertice.sh"`.

Você também pode abrir o aplicativo pelo terminal nos dois sistemas com o mesmo comando.

```text
java -jar Vertice.jar
```

Por padrão, o aplicativo abre com cadastros fictícios, um aluguel ativo com saldo e uma reserva confirmada. Para abrir a locadora sem exemplos, use `java -jar Vertice.jar --vazio`.

Os dados ficam em memória e são descartados ao fechar o aplicativo. O registro de pagamento é uma simulação local, sem conexão com banco ou operadora.

## Experimente o fluxo

1. Em **Reservas**, selecione a reserva de exemplo e clique em **Retirar veículo**. A retirada cria um registro em **Aluguéis**.
2. Selecione o aluguel recém-criado, clique em **Registrar devolução** e confirme a data.
3. Selecione o mesmo aluguel e clique em **Receber pagamento**. O formulário sugere o saldo em aberto, mas aceita pagamentos parciais.
4. Depois de quitar o saldo, selecione o aluguel e clique em **Finalizar**.

Você também pode cadastrar veículos, clientes e funcionários, confirmar ou cancelar reservas, abrir aluguéis sem reserva e estornar pagamentos antes da finalização. A agenda impede que dois compromissos confirmados ocupem o mesmo veículo nas mesmas datas.

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
