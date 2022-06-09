# MiniGroovy-Interpreter
Interpretador para uma "mini" linguagem, criada a partir de um subconjunto da linguagem Groovy.

## Tipos suportados

A linguagem possui suporte aos seguintes tipos dinâmicos:

nulo (null); <br />
lógico (false/true); <br />
strings (sequencia de caracteres entre aspas simples); <br />
arranjos (sequencia de elementos entre colchetes, separados por vírgulas); <br />
mapas (sequencia de pares (string) chave:elemento, separados por vírgulas). <br />

Os elementos de arranjos podem ser acessados como array[indice], e os elementos de mapas podem ser acessados como mapa.chave ou mapa['chave'].

## Declaracao de variaveis

Uma variável pode ser declarada como: <br />
def a <br />
em que a recebe o valor null, <br />
ou ser declarada e aribuída a um valor inicial, como: <br />
def a = 1 <br />

Varias variáveis podem ser declaradas em uma única atribuição, como: <br />
def a = 1, b, c = [k1:'um', k2:'dois'] <br />
em que a recebe 1, b recebe null e c recebe um mapa <br />
ou como <br />
def (a, b, c, d) = [1, 2, 3] <br />
em que a recebe 1, b recebe 2, c recebe 3 e d recebe null. <br />

## Comandos

if(condicao){ comandos } else { comandos } <br />
while(condicao) { comandos } <br /> 
for( declaracao/atribuicao ; condicao ; comandosDeIncremento ) { comandos } <br />
foreach( declaracao/variavel 'in' arranjo) { comandos } <br />
print(string) <br />
println(string) <br />

## Operadores:
Numéricos: + - / * % **

+: Concatenação de strings, arrranjos e mapas.
Um arranjo pode ser multiplicado por um inteiro N, resultando em uma sequencia de N vezes o arranjo.

## Operadores Lógicos:
==, <, >, <=, >=, !=: Comparam inteiros e strings. <br />
&& (and), || (or) e ! (not): operam sobre tipos lógicos. <br />
in e !in: verificam se um elemento está ou não contido em um array ou se uma string pertence ou não as chaves de um mapa. <br />

## Conversão de tipos:

Um tipo pode ser convertido para outro, utilizando o operador "as". As regras de conversão são as seguintes:

Conversão para booleano: var as Boolean <br />
null, lógico false, inteiro 0, arranjo e mapa vazios viram false; qualquer outro valor vira true\

Conversão para inteiros: var as Integer <br />
null vira 0; false vira 0 e true vira 1; inteiro é mantido; string deve ser convertida para inteiro, se falhar vira 0; qualquer outro tipo vira 0.\

Conversão para string: var as String <br />
todos os tipos, inclusive null, são convertidos para seu formato textual.

## Comutador (switch)
O switch é uma estrutura de atribuição, em que a variável recebe o valor do case correspondente a expressão avaliada.

a = switch (x) { <br />
&emsp; case null -> null <br />
&emsp; case false -> -1 <br />
&emsp; default -> 1 <br />
}

## Funções

read(string) imprime a string na tela, sem nova linha, e lê uma linha do teclado como string. \
empty(var): verificar se um arranjo, mapa ou string são vazios. \
size(var): contar a quantidade de elementos de arranjos e mapas e strings. \
keys(var): obter uma lista com todas as chaves do mapa. \
values(var): obter uma lista com todos os valores do mapa. \
