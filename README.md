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

## Database Configuration

The app persists stop words (excluded terms) across restarts using a database.

### Default: embedded H2

By default the app uses an H2 file database stored at `/tmp/newscloud/newscloud`. No configuration is needed — the schema is created automatically on first startup. This is the right choice for local development and single-container deployments where the `/tmp` directory survives restarts.

### PostgreSQL (e.g. Render)

To use a managed Postgres instance, set the following environment variables. The app will pick them up automatically via Spring Boot's property override mechanism.

| Variable                               | Example value                                        |
|----------------------------------------|------------------------------------------------------|
| `SPRING_DATASOURCE_URL`                | `jdbc:postgresql://hostname:5432/dbname`             |
| `SPRING_DATASOURCE_USERNAME`           | `myuser`                                             |
| `SPRING_DATASOURCE_PASSWORD`           | `secret`                                             |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME`  | `org.postgresql.Driver`                              |

`SPRING_DATASOURCE_DRIVER_CLASS_NAME` is optional — Spring Boot can infer the driver from the JDBC URL — but setting it explicitly avoids any ambiguity.

#### Render shortcut — `DATABASE_URL`

Render automatically injects a `DATABASE_URL` environment variable for linked databases in the format `postgres://user:password@host:5432/dbname`. The app detects this and converts it to the required `jdbc:postgresql://` form, so you can simply link the database to the service in the Render dashboard without setting any extra variables.

If you prefer to copy the connection string manually, use the **Internal Database URL** from the Render database dashboard and set it as `SPRING_DATASOURCE_URL`. Note that Render's URL starts with `postgres://` — the app normalises this automatically, but Spring Boot itself requires the `jdbc:postgresql://` prefix, so do not pass the raw URL to any other Spring property.

The JPA DDL mode is set to `update`, so existing tables are preserved and new columns are added automatically on deployment.
