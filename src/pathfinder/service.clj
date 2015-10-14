(ns pathfinder.service
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [pathfinder.analyze :as analyze]
            [pathfinder.data.data :as data]
            [pathfinder.present :as present]
            [pathfinder.query :as query]))

;;; TODO: need to add time taken to service calls

(defn build-service [data]
  (let [project-routes (routes
                        (PUT "/:project/*" {body :body {project :project path :*} :params}
                             (-> body
                                 slurp
                                 (analyze/analyze {:project project :path path})
                                 (->> (data/stash! data))
                                 present/document-stash))
                        (GET "/" {{query :q} :params}
                             (->> (query/parse query)
                                  (data/search data)
                                  present/query-results))
                        (GET "/:project" {{project :project query :q} :params}
                             (->> (query/parse query)
                                  (merge {:project project})
                                  (data/search data)
                                  present/query-results))
                        (GET "/:project/*" {{project :project path :* query :q} :params}
                             (->> (query/parse query)
                                  (merge {:project project :path path})
                                  (data/search data)
                                  ;; TODO: when only one result should present a document
                                  present/query-results)))]
    (routes
     (context "/projects" [] project-routes)
     (route/files "/" {:root "resources/public"})
     (route/not-found "Not Found"))))
