// En referens till en annan anteckning (för att "länka in" listor)
class NoteReference(
    override val id: String,
    val referencedNoteId: String,  // ID till den anteckning som refereras
    var displayTitle: String = ""   // Valfri titel att visa (cache från refererad note)
) : NoteElement {

    override fun toString(): String {
        return "NoteReference(id='$id', referencedNoteId='$referencedNoteId', displayTitle='$displayTitle')"
    }
}

