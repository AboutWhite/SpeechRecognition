
#if !defined(BOOST_PP_IS_ITERATING)

///// header body

#ifndef BOOST_MPL_QUOTE_HPP_INCLUDED
#define BOOST_MPL_QUOTE_HPP_INCLUDED

// Copyright Aleksey Gurtovoy 2000-2008
//
// Distributed under the Boost Software License, Version 1.0. 
// (See accompanying file LICENSE_1_0.txt or copy at 
// http://www.lslboost.org/LICENSE_1_0.txt)
//
// See http://www.lslboost.org/libs/mpl for documentation.

// $Id: quote.hpp 49272 2008-10-11 06:50:46Z agurtovoy $
// $Date: 2008-10-10 23:50:46 -0700 (Fri, 10 Oct 2008) $
// $Revision: 49272 $

#if !defined(BOOST_MPL_PREPROCESSING_MODE)
#   include <lslboost/mpl/void.hpp>
#   include <lslboost/mpl/aux_/has_type.hpp>
#endif

#include <lslboost/mpl/aux_/config/bcc.hpp>
#include <lslboost/mpl/aux_/config/ttp.hpp>

#if defined(BOOST_MPL_CFG_NO_TEMPLATE_TEMPLATE_PARAMETERS) \
    && !defined(BOOST_MPL_CFG_BCC590_WORKAROUNDS)
#   define BOOST_MPL_CFG_NO_QUOTE_TEMPLATE
#endif

#if !defined(BOOST_MPL_CFG_NO_IMPLICIT_METAFUNCTIONS) \
    && defined(BOOST_MPL_CFG_NO_HAS_XXX)
#   define BOOST_MPL_CFG_NO_IMPLICIT_METAFUNCTIONS
#endif

#include <lslboost/mpl/aux_/config/use_preprocessed.hpp>

#if !defined(BOOST_MPL_CFG_NO_PREPROCESSED_HEADERS) \
 && !defined(BOOST_MPL_PREPROCESSING_MODE)

#   define BOOST_MPL_PREPROCESSED_HEADER quote.hpp
#   include <lslboost/mpl/aux_/include_preprocessed.hpp>

#else

#   include <lslboost/mpl/limits/arity.hpp>
#   include <lslboost/mpl/aux_/preprocessor/params.hpp>
#   include <lslboost/mpl/aux_/config/ctps.hpp>
#   include <lslboost/mpl/aux_/config/workaround.hpp>

#   include <lslboost/preprocessor/iterate.hpp>
#   include <lslboost/preprocessor/cat.hpp>

#if !defined(BOOST_MPL_CFG_NO_QUOTE_TEMPLATE)

namespace lslboost { namespace mpl {

#if !defined(BOOST_NO_TEMPLATE_PARTIAL_SPECIALIZATION)

template< typename T, bool has_type_ >
struct quote_impl
// GCC has a problem with metafunction forwarding when T is a
// specialization of a template called 'type'.
# if BOOST_WORKAROUND(__GNUC__, BOOST_TESTED_AT(4)) \
    && BOOST_WORKAROUND(__GNUC_MINOR__, BOOST_TESTED_AT(0)) \
    && BOOST_WORKAROUND(__GNUC_PATCHLEVEL__, BOOST_TESTED_AT(2))
{
    typedef typename T::type type;
};
# else 
    : T
{
};
# endif 

template< typename T >
struct quote_impl<T,false>
{
    typedef T type;
};

#else // BOOST_NO_TEMPLATE_PARTIAL_SPECIALIZATION

template< bool > struct quote_impl
{
    template< typename T > struct result_
        : T
    {
    };
};

template<> struct quote_impl<false>
{
    template< typename T > struct result_
    {
        typedef T type;
    };
};

#endif 

#define BOOST_PP_ITERATION_PARAMS_1 \
    (3,(1, BOOST_MPL_LIMIT_METAFUNCTION_ARITY, <lslboost/mpl/quote.hpp>))
#include BOOST_PP_ITERATE()

}}

#endif // BOOST_MPL_CFG_NO_QUOTE_TEMPLATE

#endif // BOOST_MPL_CFG_NO_PREPROCESSED_HEADERS
#endif // BOOST_MPL_QUOTE_HPP_INCLUDED

///// iteration

#else
#define i_ BOOST_PP_FRAME_ITERATION(1)

template<
      template< BOOST_MPL_PP_PARAMS(i_, typename P) > class F
    , typename Tag = void_
    >
struct BOOST_PP_CAT(quote,i_)
{
    template< BOOST_MPL_PP_PARAMS(i_, typename U) > struct apply
#if defined(BOOST_MPL_CFG_BCC590_WORKAROUNDS)
    {
        typedef typename quote_impl<
              F< BOOST_MPL_PP_PARAMS(i_, U) >
            , aux::has_type< F< BOOST_MPL_PP_PARAMS(i_, U) > >::value
            >::type type;
    };
#elif !defined(BOOST_NO_TEMPLATE_PARTIAL_SPECIALIZATION)
        : quote_impl<
              F< BOOST_MPL_PP_PARAMS(i_, U) >
            , aux::has_type< F< BOOST_MPL_PP_PARAMS(i_, U) > >::value
            >
    {
    };
#else
        : quote_impl< aux::has_type< F< BOOST_MPL_PP_PARAMS(i_, U) > >::value >
            ::template result_< F< BOOST_MPL_PP_PARAMS(i_, U) > >
    {
    };
#endif
};

#undef i_
#endif // BOOST_PP_IS_ITERATING
