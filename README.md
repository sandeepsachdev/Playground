# Prompts

The prompts used to create this app, in order:

1. > delete all branches and start using main for all comments. The new project
   > should use various news feeds that are available. It should filter out
   > common words like a, this, and etc and display a word cloud based on
   > trending phrases and words.

2. > make the word cloud regenerate every 10 seconds so it looks different each
   > time. Make the words clickable so they open a news article with the keyword
   > in a new window.

3. > filter out any words containing australia, read, podcast, live, blog

4. > instead of opening a new window when you click on a word instead display a
   > dialog window should the articies with the word. Each articule should show
   > the title and short discription and should be clickable to open the
   > articles in a new window

5. > filter out the words continue people government police video free follow
   > old court donald trump email help home man talks app

6. > try to filter out any words which are not likely to be useful to show
   > trending words in news

7. > Instead of storing the filtered words in a file store them in a database.
   > If no database configured use H2 otherwise provide a way of storing the
   > filtered words in a table in a postgres database hosted on render.
   > Automatically populate the existing words into the database during
   > startup. Provide a way to exclude words that appear in the right hand
   > side list using a button. Provide a way to show and hide the right hand
   > list of top words.

8. > try to filter out any words which are not likely to be useful to show
   > trending words in news

9. > Exclude all words commonly associated with the Iran war

10. > Hide the top 25 list by default

11. > Doesn't seem like you are filtering words I exclude right at the start
    > like app

12. > Make the site better for mobile use

13. > Only use phrases not single words

14. > The ui is sometimes not responding on mobile. Especially the show top
    > words button

15. > change the resfresh to only be every 2 minutes

16. > Clicking on items works on desktop but not on mobile devices

17. > Still not working. This was working before the change to only show
    > phrases

18. > Revert back to before the change to only show phrases

19. > Show different word clouds by source. Show each word cloud for 20
    > seconds before showing the next one and iterate through them

## Database Configuration

The app persists stop words (excluded terms) across restarts using a database.

### Default: embedded H2

By default the app uses an H2 file database stored at `/tmp/newscloud/newscloud`. No configuration is needed — the schema is created automatically on first startup. This is the right choice for local development and single-container deployments where the `/tmp` directory survives restarts.

### PostgreSQL (e.g. Render)

> **Important — variable naming:** Spring Boot maps environment variables to properties by converting `UPPERCASE_UNDERSCORE` names to `lowercase.dot` form. You **must** use the uppercase form below. A variable literally named `spring.datasource.url` (with dots) will **not** be picked up.

Set these three environment variables in the Render dashboard (Environment tab):

| Render env var name            | Value                                                       |
|--------------------------------|-------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`        | `jdbc:postgresql://hostname:5432/dbname?sslmode=require`    |
| `SPRING_DATASOURCE_USERNAME`   | `myuser`                                                    |
| `SPRING_DATASOURCE_PASSWORD`   | `secret`                                                    |

The URL must start with `jdbc:postgresql://`. Use the **JDBC URI** from the Render database dashboard (not the "External Database URL" which starts with `postgres://`).

The JPA DDL mode is set to `update`, so existing tables are preserved and new columns are added automatically on deployment.
