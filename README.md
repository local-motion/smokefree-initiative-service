[![Build Status](https://travis-ci.org/local-motion/smokefree-initiative-service.svg?branch=master)](https://travis-ci.org/local-motion/smokefree-initiative-service)

## Running in Intellij

Simply run `smokefree.Application`


## Running Graphiql

```
docker run --name graphiql -d -p 4001:4000 -e API_URL=http://localhost:18085/graphql npalm/graphiql
```

Go to Graphiql at http://localhost:4001 

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