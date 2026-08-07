filepath = 'app/src/main/java/com/example/ui/components/ArticleCard.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    text = f.read()

old_sig = '''fun ArticleCard(
    article: Article,
    onArticleClick: (Article) -> Unit,
    onBookmarkToggle: (Article) -> Unit,
    onPlayAudio: (Article) -> Unit,
    modifier: Modifier = Modifier
)'''

new_sig = '''fun ArticleCard(
    article: Article,
    onArticleClick: (Article) -> Unit,
    onBookmarkToggle: (Article) -> Unit,
    onPlayAudio: (Article) -> Unit,
    onCategoryClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
)'''

target_insert = '''            // Article Honest Title (Higher prominence for Unread, dimmed for Read)'''
pill_code = '''            // Category Pill Tag
            if (article.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCategoryClick?.invoke(article.category) }
                ) {
                    Text(
                        text = "🏷️ ${article.category}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

'''

if 'onCategoryClick' not in text:
    text = text.replace(old_sig, new_sig)
    text = text.replace(target_insert, pill_code + target_insert)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(text)
    print('Updated ArticleCard with category pill')
else:
    print('Already updated ArticleCard')
