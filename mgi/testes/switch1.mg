def a = ''
def b = 11
def c = switch (a) {
  case null -> 'eh null'
  case false -> 'eh false'
  case 5 -> 'eh 5'
  case b -> 'eh 10'
  case '' -> 'eh string vazia'
  default -> 'eh nada'
}
println(c)
