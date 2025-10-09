
// A data class agora fica aqui, fora da Activity
data class BookData(val name: String, val chapters: Int)

// Criamos um 'object' (uma classe singleton) para guardar nosso mapa de dados
object BibleData {

    val mapBookIdToData = mapOf(
        1 to BookData("Gênesis", 50), 2 to BookData("Êxodo", 40), 3 to BookData("Levítico", 27),
        4 to BookData("Números", 36), 5 to BookData("Deuteronômio", 34), 6 to BookData("Josué", 24),
        7 to BookData("Juízes", 21), 8 to BookData("Rute", 4), 9 to BookData("1 Samuel", 31),
        10 to BookData("2 Samuel", 24), 11 to BookData("1 Reis", 22), 12 to BookData("2 Reis", 25),
        13 to BookData("1 Crônicas", 29), 14 to BookData("2 Crônicas", 36), 15 to BookData("Esdras", 10),
        16 to BookData("Neemias", 13), 17 to BookData("Ester", 10), 18 to BookData("Jó", 42),
        19 to BookData("Salmos", 150), 20 to BookData("Provérbios", 31), 21 to BookData("Eclesiastes", 12),
        22 to BookData("Cantares", 8), 23 to BookData("Isaías", 66), 24 to BookData("Jeremias", 52),
        25 to BookData("Lamentações", 5), 26 to BookData("Ezequiel", 48), 27 to BookData("Daniel", 12),
        28 to BookData("Oséias", 14), 29 to BookData("Joel", 3), 30 to BookData("Amós", 9),
        31 to BookData("Obadias", 1), 32 to BookData("Jonas", 4), 33 to BookData("Miquéias", 7),
        34 to BookData("Naum", 3), 35 to BookData("Habacuque", 3), 36 to BookData("Sofonias", 3),
        37 to BookData("Ageu", 2), 38 to BookData("Zacarias", 14), 39 to BookData("Malaquias", 4),
        40 to BookData("Mateus", 28), 41 to BookData("Marcos", 16), 42 to BookData("Lucas", 24),
        43 to BookData("João", 21), 44 to BookData("Atos", 28), 45 to BookData("Romanos", 16),
        46 to BookData("1 Coríntios", 16), 47 to BookData("2 Coríntios", 13), 48 to BookData("Gálatas", 6),
        49 to BookData("Efésios", 6), 50 to BookData("Filipenses", 4), 51 to BookData("Colossenses", 4),
        52 to BookData("1 Tessalonicenses", 5), 53 to BookData("2 Tessalonicenses", 3), 54 to BookData("1 Timóteo", 6),
        55 to BookData("2 Timóteo", 4), 56 to BookData("Tito", 3), 57 to BookData("Filemom", 1),
        58 to BookData("Hebreus", 13), 59 to BookData("Tiago", 5), 60 to BookData("1 Pedro", 5),
        61 to BookData("2 Pedro", 3), 62 to BookData("1 João", 5), 63 to BookData("2 João", 1),
        64 to BookData("3 João", 1), 65 to BookData("Judas", 1), 66 to BookData("Apocalipse", 22)
    )
}