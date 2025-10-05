import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class QuizCategoryResponse(
    @SerializedName("categories")
    val categories: List<Category>
)

data class Category(
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("quizzes")
    val quizzes: List<Quizze>
)

@Serializable
data class Quizze(
    @SerializedName("description")
    val description: String,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("quiz_id")
    val quizId: String,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    @SerializedName("title")
    val title: String
)
