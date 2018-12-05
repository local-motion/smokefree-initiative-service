[![Build Status](https://travis-ci.org/local-motion/smokefree-initiative-service.svg?branch=master)](https://travis-ci.org/local-motion/smokefree-initiative-service)

## Running in Intellij

Simply run `smokefree.Application` with following environment variables

```
MICRONAUT_SERVER_PORT	8086
MICRONAUT_ENVIRONMENTS	local
MYSQL_JDBC_URL	        jdbc:mysql://localhost:3306/smokefree
MYSQL_USERNAME	        root
MYSQL_PASSWORD	        root
MYSQL_DRIVER_CLASS_NAME	com.mysql.jdbc.Driver
AWS_COGNITO_USERPOOLID  eu-west-1_WsTxYUHyC
```


## Running Graphql Playground

```
brew cask install graphql-playground
```

Open the installed "GraphQL Playground" OSX application
```
./request_token.sh
```



### Mutation

In order to test a mutation, use the following query:
```
mutation CreateArticle($input: CreateArticleInput!) {
  createArticle(input: $input) {
    id
  }
}
```
And corresponding query variables:
```
{
  "input": {
    "title": "Writing GraphQL mutations with Spring boot",
    "text": "This is the text for our blog article.",
    "authorId": 1
  }
}
```
Running above example results in:
```
{
  "data": {
    "createArticle": {
      "id": "9999"
    }
  }
}
```


Other stuff:
npm install -g @aws-amplify/cli
amplify configure