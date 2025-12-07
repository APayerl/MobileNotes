// En anteckning som innehåller titel, innehåll och metadata
class Note(
    override val id: String,
    var title: String,
    var content: List<NoteElement>,
    var lastModified: Long
) : NoteElement {

    override fun toString(): String {
        return "Note(id='$id', title='$title', content=$content, lastModified=$lastModified)"
    }
}
